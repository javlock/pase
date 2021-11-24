package com.github.javlock.pase.web.crawler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.github.javlock.pase.libs.api.instance.PaseApp;
import com.github.javlock.pase.libs.data.RegExData;
import com.github.javlock.pase.libs.data.web.UpdatedUrlData;
import com.github.javlock.pase.libs.data.web.UrlData;
import com.github.javlock.pase.libs.network.Packet;
import com.github.javlock.pase.libs.network.data.DataPacket;
import com.github.javlock.pase.libs.network.data.DataPacket.ACTIONTYPE;
import com.github.javlock.pase.libs.network.data.DataPacket.PACKETTYPE;
import com.github.javlock.pase.libs.utils.web.url.UrlUtils;
import com.github.javlock.pase.web.crawler.engine.filter.FilterEngine;
import com.github.javlock.pase.web.crawler.interfaces.UrlActionInterface;
import com.github.javlock.pase.web.crawler.interfaces.WorkerEventInterface;
import com.github.javlock.pase.web.crawler.network.handler.ObjectHandlerClient;
import com.github.javlock.pase.web.crawler.storage.Storage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import lombok.Setter;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2", "AT_OPERATION_SEQUENCE_ON_CONCURRENT_ABSTRACTION" })
public class WebCrawler extends Thread {
	public static void main(String[] args) throws IOException {
		WebCrawler webCrawler = new WebCrawler();
		webCrawler.init();
		webCrawler.start();
	}

	private final @Getter PaseApp paseApp = new PaseApp();

	public final UrlActionInterface urlDetected = new UrlActionInterface() {

		@Override
		public void detected(UrlData parent, String data) {
			if (data == null || data.trim().isEmpty()) {
				return;
			}
			if (data.trim().startsWith("mailto")) { // check
													// mail
				mailDetected(parent, data);
			} else {
				// check data
				// append new
				UrlData newData = new UrlData().setUrl(data).setDomain(UrlUtils.getDomainByUrl(data)).build();

				try {
					if (!storage.contains(newData)) {
						storage.appendNew(newData);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("WebCrawler.enclosing_method():" + parent + " " + data);
					Runtime.getRuntime().exit(0);
				}
			}
		}

		@Override
		public void fileDetected(UrlData fileUrl) {
			System.err.println("file found:[" + fileUrl + "]");
		}

		@Override
		public void forbidden(UrlData data) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mailDetected(UrlData where, String email) {
			System.out.println("mailDetected():[" + where + "] [" + email + "]");
		}

		@Override
		public void notFound(UrlData fileUrl) {
			System.err.println("file not found:[" + fileUrl + "]");

		}

	};
	WorkerEventInterface workerEventInterface = new WorkerEventInterface() {

		@Override
		public void endScan(WebCrawlerWorker webCrawlerWorker) {
			UrlData urldata = webCrawlerWorker.getUrlData();
			String url = urldata.getUrl();
			String urlWithOutSession = UrlUtils.getUrlWithOutSession(url);
			boolean parsedAdded = storage.appendParsed(urlWithOutSession.hashCode());
			storage.getWorkers().remove(url, webCrawlerWorker);

			urldata.setUrl(urlWithOutSession).setDomain(UrlUtils.getDomainByUrl(urlWithOutSession)).build();
			if (parsedAdded) {

				send(new DataPacket().setType(PACKETTYPE.REQUEST).setAction(ACTIONTYPE.SAVE).setData(urldata).check());
			}
		}

	};
	private WebCrawler instanceCrawler = this;
	private final @Getter Storage storage = new Storage(this);

	private @Getter @Setter int maxThread = 25;
	private File inputFile = new File("input_url");
	private File inputFileAllow = new File("input_allow");
	private File inputFileForb = new File("input_forb");
	public final FilterEngine filter = new FilterEngine();

	private Bootstrap bootstrap;

	private NioEventLoopGroup nioEventLoopGroup;
	private ChannelFuture future;

	ConcurrentHashMap<String, WebCrawlerWorker> updateWorkers = new ConcurrentHashMap<>();

	private void init() throws IOException {
		initNetwork();

		if (!inputFile.exists()) {
			Files.createFile(inputFile.toPath());
		}
		if (!inputFileAllow.exists()) {
			Files.createFile(inputFileAllow.toPath());
		}
		if (!inputFileForb.exists()) {
			Files.createFile(inputFileForb.toPath());
		}

		List<String> listInputAllow = Files.readAllLines(inputFileAllow.toPath(), StandardCharsets.UTF_8);
		for (String allowRegEx : listInputAllow) {
			RegExData regExdata = new RegExData().setRegEx(allowRegEx).setAllow(true).setEnabled(true).build();

			filter.getAllow().put(regExdata.getId(), regExdata);
			filter.updateFilter();
		}

		List<String> listInputForb = Files.readAllLines(inputFileForb.toPath(), StandardCharsets.UTF_8);
		for (String forbRegEx : listInputForb) {
			RegExData regExdata = new RegExData().setRegEx(forbRegEx).setDeny(true).setEnabled(true).build();

			filter.getForbidden().put(regExdata.getId(), regExdata);
			filter.updateFilter();
		}

		List<String> listInput = Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8);
		for (String string : listInput) {
			if (string == null || string.trim().isEmpty()) {
				continue;
			}
			UrlData testData = new UrlData().setUrl(string).setDomain(UrlUtils.getDomainByUrl(string)).build();
			storage.appendNew(testData);

			send(new DataPacket().setType(PACKETTYPE.REQUEST).setAction(ACTIONTYPE.SAVE).setData(testData).check());

		}
	}

	private void initNetwork() {
		for (;;) {
			bootstrap = new Bootstrap();
			nioEventLoopGroup = new NioEventLoopGroup();
			bootstrap.group(nioEventLoopGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					try {
						ChannelPipeline p = ch.pipeline();

						// objects
						p.addLast(new ObjectDecoder(Integer.MAX_VALUE,
								ClassResolvers.softCachingConcurrentResolver(Packet.class.getClassLoader())));
						p.addLast(new ObjectEncoder());

						p.addLast(new ObjectHandlerClient(instanceCrawler));

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			String host = "127.0.0.1";
			int port = 6000;
			future = bootstrap.connect(host, port).awaitUninterruptibly();
			boolean result = future.isSuccess();
			if (result) {
				send(new Packet());
				break;
			}
		}
	}

	@Override
	public void run() {
		Thread.currentThread().setName("WebCrawler");
		do {
			while (storage.getWorkers().size() >= maxThread) {
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Optional<UrlData> urldataOptional = storage.next();
			if (urldataOptional.isPresent()) {
				UrlData urlData = urldataOptional.get();

				String url = urlData.getUrl();
				String withoutSession = UrlUtils.getUrlWithOutSession(url);
				if (
				// if
				// parsed
				!storage.getParsed().contains(withoutSession.hashCode())
						&& !storage.getParsed().contains(url.hashCode())
						// workers
						&& !storage.getWorkers().containsKey(url)
				// if
				) {

					WebCrawlerWorker worker = new WebCrawlerWorker();
					worker.setName(url);
					worker.setUrlData(urlData);
					worker.setUrlDetected(urlDetected);
					worker.workerEventInterface = workerEventInterface;
					storage.getWorkers().put(url, worker);
					worker.start();
				}
			}

			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}

	public void send(Object object) {
		if (future != null) {
			future.channel().writeAndFlush(object);
		}
	}

	public Optional<UpdatedUrlData> updateURL(UrlData urldata) throws InterruptedException {
		String url = urldata.getUrl();
		if (updateWorkers.containsKey(url)) {
			return Optional.empty();
		}

		if (updateWorkers.size() >= maxThread) {
			return Optional.empty();
		}
		LoggerFactory.getLogger("updateURL").info("for {}", url);

		WebCrawlerWorker worker = new WebCrawlerWorker();
		worker.setName("updateURL-" + url);
		worker.setUrlData(urldata);

		UpdatedUrlData updatedUrlData = new UpdatedUrlData();

		worker.setUrlDetected(new UrlActionInterface() {

			@Override
			public void detected(UrlData parent, String data) {
				if (data == null || data.trim().isEmpty()) {
					return;
				}
				if (data.trim().startsWith("mailto")) { // check mail
					mailDetected(parent, data);
				} else {
					// check data
					// withOutSession
					String withOutSession = UrlUtils.getUrlWithOutSession(data);
					UrlData newData = new UrlData().setUrl(data).setDomain(UrlUtils.getDomainByUrl(withOutSession))
							.build();
					// append to list
					updatedUrlData.getDetected().add(newData);
				}

			}

			@Override
			public void fileDetected(UrlData fileUrl) {
				String withOutSession = UrlUtils.getUrlWithOutSession(fileUrl.getUrl());
				fileUrl = fileUrl.setUrl(withOutSession).setDomain(UrlUtils.getDomainByUrl(withOutSession)).build();
				updatedUrlData.getFileDetected().add(fileUrl);
			}

			@Override
			public void forbidden(UrlData data) {
				String withOutSession = UrlUtils.getUrlWithOutSession(data.getUrl());
				data = data.setUrl(withOutSession).setDomain(UrlUtils.getDomainByUrl(withOutSession)).build();
				updatedUrlData.getForbidden().add(data);
			}

			@Override
			public void mailDetected(UrlData parent, String data) {
				updatedUrlData.getMailDetected().add(data);
			}

			@Override
			public void notFound(UrlData url) {
				String withOutSession = UrlUtils.getUrlWithOutSession(url.getUrl());
				url = url.setUrl(withOutSession).setDomain(UrlUtils.getDomainByUrl(withOutSession)).build();
				updatedUrlData.getNotFound().add(url);
			}
		});
		updateWorkers.put(url, worker);
		worker.start();
		worker.join();

		updatedUrlData.setNewData(worker.getUrlData());
		updateWorkers.remove(url);

		return Optional.of(updatedUrlData);
	}
}
