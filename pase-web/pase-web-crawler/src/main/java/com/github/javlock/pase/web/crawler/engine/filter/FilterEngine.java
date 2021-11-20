package com.github.javlock.pase.web.crawler.engine.filter;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javlock.pase.web.crawler.WebCrawler;
import com.github.javlock.pase.web.crawler.data.UrlData;
import com.github.javlock.pase.web.crawler.storage.Storage;

public class FilterEngine {

	private WebCrawler crawler;

	public FilterEngine(WebCrawler webCrawler) {
		crawler = webCrawler;
	}

	public boolean check(UrlData data) {
		String domain = data.getDomain();
		Storage storage = crawler.getStorage();
		CopyOnWriteArrayList<String> forb = storage.getForbidden();
		if (matchInList(domain, forb)) {
			crawler.urlDetected.forbidden(data);
			return false;
		}
		CopyOnWriteArrayList<String> allow = storage.getAllow();
		return matchInList(domain, allow);
	}

	private boolean matchInList(String domain, CopyOnWriteArrayList<String> list) {
		for (String regEx : list) {
			final Pattern pattern = Pattern.compile(regEx, Pattern.MULTILINE);
			final Matcher matcher = pattern.matcher(domain);

			while (matcher.find()) {
				String group = matcher.group(0);
				if (domain.equals(group)) {
					return true;
				}
				for (int i = 1; i <= matcher.groupCount(); i++) {
					System.out.println("Group " + i + ": " + matcher.group(i));
				}
			}
		}
		return false;
	}

}
