package com.github.javlock.pase.web.crawler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.pase.libs.data.web.UrlData;
import com.github.javlock.pase.libs.data.web.UrlData.URLTYPE;
import com.github.javlock.pase.libs.utils.web.url.UrlUtils;
import com.github.javlock.pase.web.crawler.interfaces.UrlActionInterface;
import com.github.javlock.pase.web.crawler.interfaces.WorkerEventInterface;

import lombok.Getter;
import lombok.Setter;

public class WebCrawlerWorker extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger("WebCrawlerWorker");
	private @Getter @Setter Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
	private @Setter UrlActionInterface urlDetected;
	public WorkerEventInterface workerEventInterface;

	private @Getter @Setter UrlData urlData;

	@Override
	public void run() {
		retStatusCode: {

			Response resp = null;
			try {
				urlData.setTime(System.currentTimeMillis() / 1000);
				resp = Jsoup.connect(urlData.getUrl()).proxy(proxy).userAgent("Mozilla").execute();
				Document doc = resp.parse();
				urlData.setStatusCode(resp.statusCode());
				urlData.setPageType(URLTYPE.PAGE);
				urlData.setTitle(doc.title());

				List<String> list = UrlUtils.parseDocByElementToList(doc);
				for (String newUrl : list) {
					try {
						urlDetected.detected(urlData, newUrl);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} catch (SocketTimeoutException | SocketException e) {
				// IGNORE
			} catch (HttpStatusException e) {
				int status = e.getStatusCode();
				if (status == 429) {
					LOGGER.warn("status == 429");
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					break retStatusCode;
				}

				urlData.setStatusCode(status);
				if (status == 404) {
					urlDetected.notFound(urlData);
				} else {
					System.err.println(status + ":" + urlData);
				}
			} catch (UnsupportedMimeTypeException e) {
				urlData.setPageType(URLTYPE.FILE);
				urlDetected.fileDetected(urlData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		workerEventInterface.endScan(this);
	}

}
