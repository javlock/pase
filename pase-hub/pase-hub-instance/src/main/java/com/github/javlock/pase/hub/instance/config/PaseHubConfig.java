package com.github.javlock.pase.hub.instance.config;

import com.github.javlock.pase.hub.instance.config.db.DataBaseConfig;

import lombok.Getter;
import lombok.Setter;

public class PaseHubConfig {
	private @Getter @Setter int port = 6000;

	private @Getter DataBaseConfig dbConfig = new DataBaseConfig();
	private @Getter @Setter Long timeExceeded = 3600L;
}
