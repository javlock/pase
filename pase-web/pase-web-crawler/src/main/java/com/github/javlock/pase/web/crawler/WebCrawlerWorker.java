package com.github.javlock.pase.web.crawler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class WebCrawlerWorker extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger("WebCrawlerWorker");
	private @Getter @Setter Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
	private @Setter UrlActionInterface urlDetected;
	public WorkerEventInterface workerEventInterface;

	private @Getter @Setter UrlData urlData;

	@Override
	public void run() {
		boolean needgoto = true;

		LOGGER.error("-----0 {}", urlData.getPageType());
		retStatusCode: {
			LOGGER.error("-----1 {}", urlData.getPageType());

			try {
				LOGGER.error("-----1.1 {}", urlData.getPageType());
				urlData.setTime(System.currentTimeMillis() / 1000);
				LOGGER.error("-----1.2 {}", urlData.getPageType());
				Response resp = Jsoup.connect(urlData.getUrl()).proxy(proxy).userAgent("Mozilla").execute();

				LOGGER.error("-----2 {}", urlData.getPageType());
				Document doc = resp.parse();
				LOGGER.error("-----3 {}", urlData.getPageType());

				urlData.setStatusCode(resp.statusCode());
				LOGGER.error("-----4 {}", urlData.getPageType());
				urlData.setPageType(URLTYPE.PAGE);
				LOGGER.error("-----5 {}", urlData.getPageType());

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
			} catch (javax.net.ssl.SSLHandshakeException e) {
				try {
					File dir = new File("error", "ssl");
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File logFile = new File(dir, urlData.getDomain().toUpperCase().trim());
					if (!logFile.exists()) {
						Files.createFile(logFile.toPath());
					}
					Files.writeString(logFile.toPath(), urlData.getDomain() + "\n", StandardOpenOption.APPEND);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		if (workerEventInterface != null) {
			workerEventInterface.endScan(this);
		}
	}

}
