package com.github.javlock.pase.web.filedownloader.storage;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.github.javlock.pase.libs.data.web.UrlData;

public class FDStorage {
	/**
	 * очередь
	 */
	private ConcurrentHashMap<String, UrlData> queue = new ConcurrentHashMap<>();

	public Optional<UrlData> nextUrl() {
		Optional<Entry<String, UrlData>> optional = queue.entrySet().stream().findFirst();
		if (optional.isPresent()) {
			Entry<String, UrlData> entry = optional.get();
			queue.remove(entry.getKey(), entry.getValue());
			return Optional.of(entry.getValue());
		}
		return Optional.empty();
	}

}
