package com.github.javlock.pase.hub.instance;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.javlock.pase.hub.instance.config.PaseHubConfig;
import com.github.javlock.pase.hub.instance.db.DataBase;
import com.github.javlock.pase.hub.instance.network.handler.ObjectHandlerServer;
import com.github.javlock.pase.hub.instance.service.ConfigurationUpdater;
import com.github.javlock.pase.hub.instance.storage.PaseHubStorage;
import com.github.javlock.pase.libs.Balancer;
import com.github.javlock.pase.libs.api.instance.Parameter;
import com.github.javlock.pase.libs.api.instance.PaseApp;
import com.github.javlock.pase.libs.data.web.UrlData;
import com.github.javlock.pase.libs.network.Packet;
import com.github.javlock.pase.libs.network.data.DataPacket;
import com.github.javlock.pase.libs.network.data.DataPacket.ACTIONTYPE;
import com.github.javlock.pase.libs.network.data.DataPacket.PACKETTYPE;
import com.github.javlock.pase.libs.utils.io.IOUtils;
import com.github.javlock.pase.web.crawler.engine.filter.FilterEngine;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class PaseHub extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger("PaseHub");

	public static void main(String[] args) throws InterruptedException, IOException, SQLException {
		PaseHub hub = new PaseHub();
		hub.init();
		hub.start();
		hub.join();
	}

	private PaseHub instanceHub = this;

	private final @Getter PaseApp paseApp = new PaseApp();
	private final @Getter FilterEngine filterEngine = new FilterEngine();

	private final @Getter Balancer balancer = new Balancer();

	private final @Getter PaseHubStorage storage = new PaseHubStorage();
	private final @Getter DataBase db = new DataBase(instanceHub);

	private @Getter ServerBootstrap serverBootstrap = new ServerBootstrap();
	private @Getter EventLoopGroup serverWorkgroup = new NioEventLoopGroup();
	private @Getter ChannelFuture bindChannelFuture;

	private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
	private @Getter PaseHubConfig config = new PaseHubConfig();
	private final File configFile = new File("config.yaml");

	ConfigurationUpdater configUpdater = new ConfigurationUpdater(instanceHub);

	/**
	 * sending an object to all connected clients
	 *
	 *
	 * <b> >>dangerous method that does not check authorization and client type<<
	 *
	 * @param ctx [ChannelHandlerContext context] to exclude from mailing* list
	 * @param msg object to send
	 */
	public void broadcast(ChannelHandlerContext ctx, Serializable msg) {
		for (ChannelHandlerContext context : getStorage().getClients()) {
			if (ctx != null && context.equals(ctx)) {
				continue;
			}
			balancer.netxtObj();
			context.writeAndFlush(msg);
		}
	}

	private void init() throws IOException, SQLException {
		readConfig();

		String version = null;

		String[] list = new String(IOUtils.getFileFromJarAsBytes("pase-hub-instance-git.properties"),
				StandardCharsets.UTF_8).split("\n");
		for (String line : list) {
			if (line.toLowerCase().startsWith("git.commit.id.full".toLowerCase())) {
				version = line.split("=")[1];
			}
		}

		paseApp.appType(Parameter.PaseAppType.HUB).version(version).init();

		balancer.setObjCountMax(10000);

		db.init();
		initNetwork();
	}

	private void initNetwork() {
		serverBootstrap.group(serverWorkgroup).channel(NioServerSocketChannel.class)
				.localAddress(new InetSocketAddress(config.getPort()));
		serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);

		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();

				p.addLast(new ObjectDecoder(Integer.MAX_VALUE,
						ClassResolvers.softCachingConcurrentResolver(Packet.class.getClassLoader())));
				p.addLast(new ObjectEncoder());

				p.addLast(new ObjectHandlerServer(paseApp, instanceHub));
			}
		});
	}

	private void readConfig() throws IOException {
		if (!configFile.exists()) {
			objectMapper.writeValue(configFile, config);
			LOGGER.info("edit {}", configFile);
			Runtime.getRuntime().exit(0);
		} else {
			config = objectMapper.readValue(configFile, PaseHubConfig.class);
		}
	}

	@Override
	public void run() {
		Thread.currentThread().setName("PaseHub");
		configUpdater.start();

		bindChannelFuture = serverBootstrap.bind();

		do {

			try {

				// FILES
				List<UrlData> links = db.getUrlFilesNoParsed();
				System.err.println(links.size());
				for (UrlData urlData : links) {

				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			// URL UPDATE
			try {
				List<UrlData> urlTimeExceeded = db.getUrlTimeExceeded();
				for (UrlData urlData : urlTimeExceeded) {

					broadcast(null, new DataPacket()
							//
							.setData(urlData).setType(PACKETTYPE.REQUEST).setAction(ACTIONTYPE.UPDATE)
							//
							.check());
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			// URL NEW

			try {
				List<UrlData> urlTimeExceeded = db.getUrlNew();
				for (UrlData urlData : urlTimeExceeded) {
					broadcast(null, new DataPacket()
							//
							.setData(urlData).setType(PACKETTYPE.REQUEST).setAction(ACTIONTYPE.UPDATE)
							//
							.check());

				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}
}
