package com.github.javlock.pase.hub.instance.storage;

import java.util.concurrent.CopyOnWriteArrayList;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

public class PaseHubStorage {
	private final @Getter CopyOnWriteArrayList<ChannelHandlerContext> clients = new CopyOnWriteArrayList<>();

	public void connected(ChannelHandlerContext ctx) {
		clients.addIfAbsent(ctx);
	}

	public void disconnected(ChannelHandlerContext ctx) {
		clients.remove(ctx);
	}

}
