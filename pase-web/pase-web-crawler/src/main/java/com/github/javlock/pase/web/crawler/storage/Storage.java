package com.github.javlock.pase.web.crawler.storage;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.javlock.pase.web.crawler.WebCrawler;
import com.github.javlock.pase.web.crawler.WebCrawlerWorker;
import com.github.javlock.pase.web.crawler.data.UrlData;

import lombok.Getter;

public class Storage {
	class StorageElement {
		private String domain;
		private final CopyOnWriteArrayList<UrlData> urls = new CopyOnWriteArrayList<>();

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StorageElement other = (StorageElement) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return Objects.equals(domain, other.domain) && Objects.equals(urls, other.urls);
		}

		private Storage getEnclosingInstance() {
			return Storage.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + Objects.hash(domain, urls);
			return result;
		}

		@Override
		public String toString() {
			return "StorageElement [domain=" + domain + ", urls=" + urls + "]";
		}
	}

	private @Getter CopyOnWriteArrayList<String> forbidden = new CopyOnWriteArrayList<>();
	private @Getter CopyOnWriteArrayList<String> allow = new CopyOnWriteArrayList<>();

	private @Getter ConcurrentHashMap<String, WebCrawlerWorker> workers = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<String, StorageElement> urlMap = new ConcurrentHashMap<>();

	private @Getter CopyOnWriteArrayList<Integer> parsed = new CopyOnWriteArrayList<>();

	private WebCrawler crawler;

	public Storage(WebCrawler webCrawler) {
		crawler = webCrawler;
	}

	public void appendNew(UrlData data) throws IllegalArgumentException {

		// filter
		if (!crawler.filter.check(data)) {
			return;
		}

		if (data.isBuilded()) {
			String domain = data.getDomain();
			if (domain == null) {
				throw new IllegalArgumentException("domain == null");
			}
			StorageElement element = urlMap.computeIfAbsent(domain, a -> new StorageElement());
			element.urls.addIfAbsent(data);
		} else {
			System.out.println("Storage.appendNew():" + data);
		}
	}

	public boolean appendParsed(int hashCode) {
		if (!parsed.contains(hashCode)) {
			parsed.add(hashCode);
			return true;
		}
		return false;
	}

	public boolean contains(UrlData data) {
		String domain = data.getDomain();
		if (!urlMap.containsKey(domain)) {
			return false;
		}
		StorageElement element = urlMap.get(domain);
		return element.urls.contains(data);
	}

	public Optional<UrlData> next() {
		Optional<Entry<String, StorageElement>> elementOptional = urlMap.entrySet().stream().findFirst();
		if (elementOptional.isPresent()) {

			Entry<String, StorageElement> elementEntry = elementOptional.get();
			String domain = elementEntry.getKey();
			StorageElement value = elementEntry.getValue();

			CopyOnWriteArrayList<UrlData> urlList = value.urls;
			Optional<UrlData> urlDataOptional = urlList.stream().findFirst();
			if (urlDataOptional.isPresent()) {
				urlList.remove(urlDataOptional.get());
				if (urlList.isEmpty()) {
					// TODO
					urlMap.remove(domain, value);
				}
				return urlDataOptional;
			}
		}
		return Optional.empty();
	}

}
