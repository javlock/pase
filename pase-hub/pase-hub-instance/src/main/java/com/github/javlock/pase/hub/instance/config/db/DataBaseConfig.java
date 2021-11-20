package com.github.javlock.pase.hub.instance.config.db;

import lombok.Getter;
import lombok.Setter;

public class DataBaseConfig {
	private @Getter @Setter String host = "127.0.0.1";
	private @Getter @Setter int port = 5432;
	private @Getter @Setter boolean ssl;

	private @Getter @Setter String user = "pase-user";
	private @Getter @Setter String password = "pase-password-" + System.currentTimeMillis();

	private @Getter @Setter String dataBaseName = "pase-db";

}
