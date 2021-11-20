package com.github.javlock.pase.web.crawler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketException;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

import com.github.javlock.pase.web.crawler.data.UrlData;
import com.github.javlock.pase.web.crawler.interfaces.UrlActionInterface;
import com.github.javlock.pase.web.crawler.interfaces.WorkerEventInterface;
import com.github.javlock.pase.web.crawler.utils.url.UrlUtils;

import lombok.Getter;
import lombok.Setter;

public class WebCrawlerWorker extends Thread {
	private @Getter @Setter Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
	private @Setter UrlActionInterface urlDetected;
	public WorkerEventInterface workerEventInterface;
	private @Getter @Setter UrlData urlData;

	@Override
	public void run() {

		Response resp = null;
		try {
			resp = Jsoup.connect(urlData.getUrl()).proxy(proxy).userAgent("Mozilla").execute();
			Document doc = resp.parse();
			urlData.setTitle(doc.title());

			List<String> list = UrlUtils.parseDocByElementToList(doc);
			for (String newUrl : list) {
				try {
					urlDetected.detected(urlData, newUrl);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (SocketException e) {
			// IGNORE
		} catch (HttpStatusException e) {
			int status = e.getStatusCode();
			if (status == 404) {
				urlDetected.notFound(urlData);
			} else {
				System.err.println(status + ":" + urlData);
			}
		} catch (UnsupportedMimeTypeException e) {
			urlDetected.fileDetected(urlData);
		} catch (IOException e) {
			e.printStackTrace();
		}

		workerEventInterface.endScan(this);
	}

}
