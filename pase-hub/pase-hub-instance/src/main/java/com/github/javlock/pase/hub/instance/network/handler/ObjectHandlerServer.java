package com.github.javlock.pase.hub.instance.network.handler;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.pase.hub.instance.PaseHub;
import com.github.javlock.pase.web.crawler.data.SavePacket;
import com.github.javlock.pase.web.crawler.data.UrlData;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class ObjectHandlerServer extends ChannelDuplexHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger("ObjectHandlerServer");
	private PaseHub hub;

	public ObjectHandlerServer(PaseHub instanceHub) {
		hub = instanceHub;
	}

	private void broadcast(Serializable msg) {
		if (hub.getBindChannelFuture() != null) {
			hub.getBindChannelFuture().channel().writeAndFlush(msg);
		}
	}

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
		if (msg instanceof SavePacket savePacket) {
			Serializable data = savePacket.getData();
			if (data instanceof UrlData urldata) {
				hub.getDb().saveUrlData(urldata);
			} else {
				LOGGER.warn("data class:[{}] data:[{}]", data.getClass().getSimpleName(), data);
				return;
			}
			broadcast((Serializable) msg);
			return;
		}

		LOGGER.info("msg class:[{}] data:[{}]", msg.getClass().getSimpleName(), msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("error", cause);
	}
}
