package com.github.javlock.pase.web.crawler.network.handler;

import java.io.Serializable;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.pase.libs.data.web.UpdatedUrlData;
import com.github.javlock.pase.libs.data.web.UrlData;
import com.github.javlock.pase.libs.network.PaseObjectHandler;
import com.github.javlock.pase.libs.network.data.DataPacket;
import com.github.javlock.pase.libs.network.data.DataPacket.ACTIONTYPE;
import com.github.javlock.pase.libs.network.data.DataPacket.PACKETTYPE;
import com.github.javlock.pase.web.crawler.WebCrawler;

import io.netty.channel.ChannelHandlerContext;

public class ObjectHandlerClient extends PaseObjectHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger("ObjectHandlerClient");

	private WebCrawler crawler;

	public ObjectHandlerClient(WebCrawler instanceCrawler) {
		crawler = instanceCrawler;
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
		if (msg instanceof DataPacket dataPacket) {
			PACKETTYPE type = dataPacket.getType();
			Serializable data = dataPacket.getData();
			ACTIONTYPE action = dataPacket.getAction();
			if (type.equals(PACKETTYPE.REQUEST)) {
				if (action.equals(ACTIONTYPE.UPDATE)) {
					if (data instanceof UrlData urldata) {
						urldata.build();
						new Thread(() -> {
							try {
								Optional<UpdatedUrlData> updatedUrlDataoOptional = crawler.updateURL(urldata);
								if (updatedUrlDataoOptional.isPresent()) {
									ctx.writeAndFlush(updatedUrlDataoOptional.get());
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}).start();
						return;
					} else {
						LOGGER.info("data class:[{}] data:[{}]", data.getClass().getSimpleName(), data);
					}
				}

			}

		}

		LOGGER.info("msg class:[{}] msg:[{}]", msg.getClass().getSimpleName(), msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("error", cause);
	}
}
