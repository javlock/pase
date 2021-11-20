package com.github.javlock.pase.web.crawler.data;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class SavePacket implements Serializable {
	private static final long serialVersionUID = 1224244819560036482L;
	private @Getter @Setter Serializable data;
}
