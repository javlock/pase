package com.github.javlock.pase.hub.instance.network.handler;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.pase.hub.instance.PaseHub;
import com.github.javlock.pase.libs.api.instance.PaseApp;
import com.github.javlock.pase.libs.data.web.UrlData;
import com.github.javlock.pase.libs.network.PaseAppInitPacket;
import com.github.javlock.pase.libs.network.PaseObjectHandler;
import com.github.javlock.pase.libs.network.data.DataPacket;

import io.netty.channel.ChannelHandlerContext;

public class ObjectHandlerServer extends PaseObjectHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger("ObjectHandlerServer");
	private PaseHub hub;

	public ObjectHandlerServer(PaseApp paseApp, PaseHub instanceHub) {
		setApp(paseApp);
		hub = instanceHub;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("connected {}", ctx.channel().remoteAddress());
		hub.getStorage().connected(ctx);

		// TODO запросить информацию

		PaseAppInitPacket packet = new PaseAppInitPacket(hub.getPaseApp().forInitPacket());
		ctx.writeAndFlush(packet);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("disconnected {}", ctx.channel().remoteAddress());
		hub.getStorage().disconnected(ctx);
		// TODO удалить запись
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof DataPacket savePacket) {

			// TODO CHECK
			Serializable data = savePacket.getData();
			if (data instanceof UrlData urldata) {
				hub.getDb().saveUrlData(urldata);
			} else {
				LOGGER.warn("data class:[{}] data:[{}]", data.getClass().getSimpleName(), data);
				return;
			}
			hub.broadcast(ctx, (Serializable) msg);
			return;
		}
		LOGGER.info("msg class:[{}] data:[{}]", msg.getClass().getSimpleName(), msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("error", cause);
	}
}
