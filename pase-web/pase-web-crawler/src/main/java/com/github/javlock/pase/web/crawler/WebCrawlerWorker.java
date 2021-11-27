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

import com.github.javlock.pase.libs.data.email.EmailData;
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
		boolean needskip = UrlUtils.isOldTorProto(urlData);

		if (needskip) {
			urlData.setTime(System.currentTimeMillis() / 1000);
			urlData.setPageType(URLTYPE.OLDPROTO);
		} else {
			retStatusCode: {
				try {
					urlData.setTime(System.currentTimeMillis() / 1000);
					Response resp = Jsoup.connect(urlData.getUrl()).proxy(proxy).userAgent("Mozilla").execute();
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
					List<Object> listText = UrlUtils.parseDocByTextToList(doc);
					for (Object obj : listText) {
						try {
							if (obj instanceof UrlData) {
								urlDetected.detected(urlData, ((UrlData) obj).getUrl());
							}
							if (obj instanceof EmailData) {
								urlDetected.mailDetected(urlData, ((EmailData) obj).getEmail());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (SocketTimeoutException e) {
					urlData.setPageType(URLTYPE.SOCKETTIMEOUTEXCEPTION);
				} catch (SocketException e) {
					urlData.setPageType(URLTYPE.SOCKETEXCEPTION);
					e.printStackTrace();
				} catch (HttpStatusException e) {
					int status = e.getStatusCode();
					urlData.setStatusCode(status);

					if (status == 429) {
						LOGGER.warn("status == 429");
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						break retStatusCode;
					} else if (status == 404) {
						urlData.setPageType(URLTYPE.NOTFOUND);
						urlDetected.notFound(urlData);
					} else if (status == 403) {
						urlData.setPageType(URLTYPE.FORBIDDEN);
						urlDetected.forbidden(urlData);
					} else {
						LOGGER.warn("status [{}] ", status);
					}

				} catch (UnsupportedMimeTypeException e) {
					urlData.setPageType(URLTYPE.FILE);
					urlDetected.fileDetected(urlData);
				} catch (javax.net.ssl.SSLHandshakeException e) {
					urlData.setPageType(URLTYPE.SSLHANDSHAKEEXCEPTION);
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

		}

		if (workerEventInterface != null) {
			workerEventInterface.endScan(this);
		}
	}

}
