package com.github.javlock.pase.hub.instance;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.javlock.pase.hub.instance.config.PaseHubConfig;
import com.github.javlock.pase.hub.instance.db.DataBase;
import com.github.javlock.pase.hub.instance.network.handler.ObjectHandlerServer;
import com.github.javlock.pase.web.crawler.data.Packet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
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

public class PaseHub extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger("PaseHub");

	public static void main(String[] args) throws InterruptedException, IOException, SQLException {
		PaseHub hub = new PaseHub();
		hub.init();
		hub.start();
		hub.join();
	}

	PaseHub instanceHub = this;

	private final @Getter DataBase db = new DataBase(instanceHub);

	private @Getter ServerBootstrap serverBootstrap = new ServerBootstrap();
	private @Getter EventLoopGroup serverWorkgroup = new NioEventLoopGroup();
	private @Getter ChannelFuture bindChannelFuture;

	private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
	private @Getter PaseHubConfig config = new PaseHubConfig();
	private final File configFile = new File("config.yaml");

	private void init() throws IOException, SQLException {
		readConfig();

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

				p.addLast(new ObjectHandlerServer(instanceHub));
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
		bindChannelFuture = serverBootstrap.bind();
	}

}
