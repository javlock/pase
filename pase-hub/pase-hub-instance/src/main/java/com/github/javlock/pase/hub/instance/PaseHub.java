package com.github.javlock.pase.hub.instance;

import java.net.InetSocketAddress;

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
	private static @Getter ServerBootstrap serverBootstrap = new ServerBootstrap();
	private static @Getter EventLoopGroup serverWorkgroup = new NioEventLoopGroup();

	public static void main(String[] args) throws InterruptedException {
		PaseHub hub = new PaseHub();
		hub.init();
		hub.start();
		hub.join();
	}

	PaseHub instanceHub = this;
	private final @Getter DataBase db = new DataBase();

	private int port = 6000;
	private @Getter ChannelFuture bindChannelFuture;

	private void init() {
		initDataBase();
		initNetwork();
	}

	private void initDataBase() {

	}

	private void initNetwork() {
		serverBootstrap.group(serverWorkgroup).channel(NioServerSocketChannel.class)
				.localAddress(new InetSocketAddress(port));
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

	@Override
	public void run() {
		Thread.currentThread().setName("PaseHub");
		bindChannelFuture = serverBootstrap.bind();
	}

}
