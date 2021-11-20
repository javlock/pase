package com.github.javlock.pase.web.crawler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import com.github.javlock.pase.web.crawler.data.Packet;
import com.github.javlock.pase.web.crawler.data.SavePacket;
import com.github.javlock.pase.web.crawler.data.UrlData;
import com.github.javlock.pase.web.crawler.engine.filter.FilterEngine;
import com.github.javlock.pase.web.crawler.interfaces.UrlActionInterface;
import com.github.javlock.pase.web.crawler.interfaces.WorkerEventInterface;
import com.github.javlock.pase.web.crawler.network.handler.ObjectHandlerClient;
import com.github.javlock.pase.web.crawler.storage.Storage;
import com.github.javlock.pase.web.crawler.utils.url.UrlUtils;

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

public class WebCrawler extends Thread {

	public static void main(String[] args) throws IOException {
		WebCrawler webCrawler = new WebCrawler();
		webCrawler.init();
		webCrawler.start();
	}

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

			if (parsedAdded) {
				SavePacket packet = new SavePacket();

				UrlData forSendUrlData = new UrlData().setUrl(urlWithOutSession)
						.setDomain(UrlUtils.getDomainByUrl(urlWithOutSession)).build();

				forSendUrlData.setPageType(urldata.getPageType());
				forSendUrlData.setTitle(urldata.getTitle());

				packet.setData(forSendUrlData);
				send(packet);
			}
		}

	};

	private final @Getter Storage storage = new Storage(this);

	private int maxThread = 25;
	private File inputFile = new File("input_url");
	private File inputFileAllow = new File("input_allow");
	private File inputFileForb = new File("input_forb");
	public final FilterEngine filter = new FilterEngine(this);

	private Bootstrap bootstrap;

	private NioEventLoopGroup nioEventLoopGroup;
	private ChannelFuture future;

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
			storage.getAllow().addIfAbsent(allowRegEx);
		}

		List<String> listInputForb = Files.readAllLines(inputFileForb.toPath(), StandardCharsets.UTF_8);
		for (String forbRegEx : listInputForb) {
			storage.getForbidden().addIfAbsent(forbRegEx);
		}

		List<String> listInput = Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8);
		for (String string : listInput) {
			if (string == null || string.trim().isEmpty()) {
				continue;
			}
			UrlData testData = new UrlData().setUrl(string).setDomain(UrlUtils.getDomainByUrl(string)).build();
			storage.appendNew(testData);
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

						p.addLast(new ObjectHandlerClient());

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
}
