package com.github.javlock.pase.web.crawler.data;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class FileData implements Serializable {
	private static final long serialVersionUID = 9166409222768532913L;
	private @Getter String sha256;
	private @Getter String sha512;
	private @Getter @Setter byte[] data;
}
