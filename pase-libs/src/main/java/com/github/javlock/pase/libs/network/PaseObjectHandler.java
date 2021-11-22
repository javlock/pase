package com.github.javlock.pase.libs.network;

import com.github.javlock.pase.libs.api.instance.PaseApp;

import io.netty.channel.ChannelDuplexHandler;
import lombok.Getter;
import lombok.Setter;

public class PaseObjectHandler extends ChannelDuplexHandler {
	private @Getter @Setter PaseApp app;
}
