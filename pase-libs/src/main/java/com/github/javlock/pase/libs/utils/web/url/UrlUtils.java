package com.github.javlock.pase.libs.utils.web.url;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.pase.libs.data.web.UrlData;

import lombok.NonNull;

public class UrlUtils {
	public enum SESSIONKEYS {
		SID, PHPSESSID;

		static boolean containsKey(String inputKey) {
			String keyUC = inputKey.toUpperCase();
			for (SESSIONKEYS key : SESSIONKEYS.values()) {
				if (key.toString().equals(keyUC)) {
					return true;
				}
			}
			return false;
		}

	}

	private static final Logger LOGGER = LoggerFactory.getLogger("UrlUtils");

	private static final String ABSHREF = "abs:href";

	// static final String = ":\\/\\/(([-A-Za-z0-9]*\\.){1,}[-A-Za-z0-9]*)";

	private static final String REGEXDOMAIN =
			// 2 proto
			// 3 domain
			// 5 port
			// 6 path
			// 7 params
			"((ftp|htt[ps]{1,2}):\\/\\/)?([-a-z0-9.а-я]{1,}\\.[a-z0-9а-я]{1,})([:0-9]{0,})([-a-zA-Z0-9а-я\\/.=&\\%\\ +]*)?([?a-zA-Z0-9а-я+=&%]*)?";

	public static String getDomainByUrl(String url) {
		final Pattern pattern = Pattern.compile(REGEXDOMAIN, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(url.toLowerCase());
		String group = null;
		while (matcher.find()) {
			group = matcher.group(3);// 3 domain
			if (group != null) {
				break;
			}
		}
		if (group == null) {
			throw new IllegalArgumentException(String.format("group(1)==null for url:[%s]", url));
		}
		return group;
	}

	public static String getUrlWithOutSession(String url) {
		if (url.contains("?")) {// args present

			String[] arr = url.split("\\?");
			StringBuilder urlBuilder = new StringBuilder();
			if (arr.length == 2) {
				ArrayList<String> listArgs = new ArrayList<>();

				String path = arr[0];
				urlBuilder.append(path);
				String args = arr[1];

				String[] argsArr = args.split("&");

				for (String argPair : argsArr) {
					String[] argPairArr = argPair.split("=");
					String keyUC = argPairArr[0].toUpperCase();

					if (!SESSIONKEYS.containsKey(keyUC)) {
						listArgs.add(argPair);
					}
				}

				if (!listArgs.isEmpty()) {
					urlBuilder.append('?');
				}
				int index = 0;
				for (String arg : listArgs) {
					if (listArgs.size() == 1 || index == listArgs.size() - 1) {
						urlBuilder.append(arg);
					} else {
						urlBuilder.append(arg).append("&");
					}
					index++;
				}
				if (!urlBuilder.toString().equalsIgnoreCase(url)) {
					url = urlBuilder.toString();
				}
			} else {
				throw new IllegalArgumentException("count ? :" + arr.length);
			}
		}
		return url;
	}

	/**
	 *
	 * @param urlData
	 * @return true if domain length 22 (v2) and false to other (v3 length=62)
	 */
	public static boolean isOldTorProto(UrlData urlData) {
		String domain = urlData.getDomain().toLowerCase();
		return domain.endsWith(".onion") && domain.length() == 22;
	}

	public static List<String> parseDocByElementToList(@NonNull Document doc) {
		ArrayList<String> answ = new ArrayList<>();
		Elements a = doc.select("a");
		for (Element element : a) {
			String aaa = null;
			aaa = element.absUrl(ABSHREF);

			if (!answ.contains(aaa)) {
				answ.add(aaa);
			}
		}
		Elements link = doc.select("link");
		for (Element element : link) {
			String linkUrl = null;
			linkUrl = element.absUrl(ABSHREF);
			if (!answ.contains(linkUrl)) {
				answ.add(linkUrl);
			}
		}

		Collections.sort(answ);
		return answ;
	}

	public static List<Object> parseDocByTextToList(Document doc) {
		ArrayList<Object> answ = new ArrayList<>();
		Element body = doc.body();
		if (body == null) {
			return answ;
		}
		String text = body.text();

		// debug
		// LOGGER.error(text);
		File dir = new File("url-utils", UrlUtils.getDomainByUrl(Thread.currentThread().getName()));
		try {
			if (!dir.exists()) {
				Files.createDirectories(dir.toPath());
			}
			File file = new File(dir, "log");
			if (!file.exists()) {
				Files.createFile(file.toPath());
			}
			Files.writeString(file.toPath(), text + "\n", StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// debug

		// MAILS
		if (text.contains("@")) {
			// TODO MAIL
		}

		return answ;

	}

}
