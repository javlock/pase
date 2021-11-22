package com.github.javlock.pase.libs.utils.web.url;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.NonNull;

public class UrlUtils {

	public enum SESSIONKEYS {
		SID;

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

	private static final String ABSHREF = "abs:href";

	static final String REGEXDOMAIN = ":\\/\\/(([-A-Za-z0-9]*\\.){1,}[-A-Za-z0-9]*)";

	public static String getDomainByUrl(String url) {
		final Pattern pattern = Pattern.compile(REGEXDOMAIN, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(url);
		String group = null;
		while (matcher.find()) {
			group = matcher.group(1);
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

}
