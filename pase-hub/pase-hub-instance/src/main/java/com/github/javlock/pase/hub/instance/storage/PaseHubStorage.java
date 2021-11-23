package com.github.javlock.pase.hub.instance.storage;

import java.util.concurrent.CopyOnWriteArrayList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP" })
public class PaseHubStorage {
	private final @Getter CopyOnWriteArrayList<ChannelHandlerContext> clients = new CopyOnWriteArrayList<>();

	public void connected(ChannelHandlerContext ctx) {
		clients.addIfAbsent(ctx);
	}

	public void disconnected(ChannelHandlerContext ctx) {
		clients.remove(ctx);
	}

}
