package com.github.javlock.pase.web.crawler.interfaces;

import com.github.javlock.pase.web.crawler.data.UrlData;

public interface UrlActionInterface {

	void detected(UrlData url, String newUrl);

	void fileDetected(UrlData fileUrl);

	void forbidden(UrlData data);

	void mailDetected(UrlData parent, String data);

	void notFound(UrlData url);
}
