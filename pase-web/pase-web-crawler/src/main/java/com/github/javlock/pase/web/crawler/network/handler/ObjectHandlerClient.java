package com.github.javlock.pase.web.crawler.network.handler;

import java.io.Serializable;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.pase.libs.data.web.UpdatedUrlData;
import com.github.javlock.pase.libs.data.web.UrlData;
import com.github.javlock.pase.libs.network.ConfigurationUpdatePacket;
import com.github.javlock.pase.libs.network.PaseObjectHandler;
import com.github.javlock.pase.libs.network.data.DataPacket;
import com.github.javlock.pase.libs.network.data.DataPacket.ACTIONTYPE;
import com.github.javlock.pase.libs.network.data.DataPacket.PACKETTYPE;
import com.github.javlock.pase.web.crawler.WebCrawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.channel.ChannelHandlerContext;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
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
		if (msg instanceof DataPacket) {
			DataPacket dataPacketObj = (DataPacket) msg;

			PACKETTYPE type = dataPacketObj.getType();
			Serializable data = dataPacketObj.getData();
			ACTIONTYPE action = dataPacketObj.getAction();

			if (type.equals(PACKETTYPE.REQUEST)) {
				if (action.equals(ACTIONTYPE.UPDATE)) {
					if (data instanceof UrlData) {
						UrlData urldataObj = (UrlData) data;
						urldataObj.build();
						new Thread(() -> {
							try {
								Optional<UpdatedUrlData> updatedUrlDataoOptional = crawler.updateURL(urldataObj);
								if (updatedUrlDataoOptional.isPresent()) {
									UpdatedUrlData val = updatedUrlDataoOptional.get();

									LOGGER.info("");
									LOGGER.info("");
									LOGGER.info("");
									LOGGER.info("");
									LOGGER.info("");
									LOGGER.info("");
									LOGGER.info("");

									StringBuilder builder = new StringBuilder();
									builder.append("data:").append(val.getNewData()).append('\n');

									for (UrlData detected : val.getDetected()) {
										builder.append("detected:").append(detected).append('\n');
									}
									for (UrlData fileDetected : val.getFileDetected()) {
										builder.append("fileDetected:").append(fileDetected).append('\n');

									}
									for (UrlData forbidden : val.getForbidden()) {
										builder.append("forbidden:").append(forbidden).append('\n');

									}
									for (String mailDetected : val.getMailDetected()) {
										builder.append("mailDetected:").append(mailDetected).append('\n');

									}
									for (UrlData notfound : val.getNotFound()) {
										builder.append("notfound:").append(notfound).append('\n');
									}
									LOGGER.info("{}", builder);

									ctx.writeAndFlush(val);
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
		if (msg instanceof ConfigurationUpdatePacket) {
			ConfigurationUpdatePacket configurationUpdatePacket = (ConfigurationUpdatePacket) msg;

			if (!configurationUpdatePacket.getFilterRegExDatas().isEmpty()) {
				crawler.filter.updateFilter(configurationUpdatePacket.getFilterRegExDatas());
			}
			if (configurationUpdatePacket.getThreadMax() != null) {
				crawler.setMaxThread(configurationUpdatePacket.getThreadMax());
			}
			return;
		}

		LOGGER.info("msg class:[{}] msg:[{}]", msg.getClass().getSimpleName(), msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("error", cause);
	}
}
