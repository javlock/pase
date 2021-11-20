package com.github.javlock.pase.web.filedownloader.storage;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.github.javlock.pase.web.crawler.data.UrlData;

public class FDStorage {
	/**
	 * очередь
	 */
	private ConcurrentHashMap<String, UrlData> queue = new ConcurrentHashMap<>();

	public Optional<UrlData> nextUrl() {

		return Optional.empty();
	}

}
