package com.github.javlock.pase.web.crawler.network.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class ObjectHandlerClient extends ChannelDuplexHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger("ObjectHandlerClient");

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("connected {}", ctx.channel().remoteAddress());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("disconnected {}", ctx.channel().remoteAddress());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		LOGGER.info("msg class:[{}] data:[{}]", msg.getClass().getSimpleName(), msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("error", cause);
	}
}
