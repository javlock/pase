package com.github.javlock.pase.web.crawler.engine.filter;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.pase.libs.data.RegExData;
import com.github.javlock.pase.libs.data.web.UrlData;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class FilterEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger("FilterEngine");

	private @Getter ConcurrentHashMap<String, RegExData> forbidden = new ConcurrentHashMap<>();
	private @Getter ConcurrentHashMap<String, RegExData> allow = new ConcurrentHashMap<>();

	public boolean check(UrlData data) {
		String domain = data.getDomain();
		if (matchInList(domain, forbidden)) {
			return false;
		}
		return matchInList(domain, allow);
	}

	private boolean matchInList(String domain, ConcurrentHashMap<String, RegExData> map) {
		for (Entry<String, RegExData> entry : map.entrySet()) {
			RegExData val = entry.getValue();
			String regEx = val.getRegEx();

			final Pattern pattern = Pattern.compile(regEx, Pattern.MULTILINE);
			final Matcher matcher = pattern.matcher(domain);

			while (matcher.find()) {
				String group = matcher.group(0);
				if (domain.equals(group)) {
					return true;
				}
				for (int i = 1; i <= matcher.groupCount(); i++) {
					String groupI = matcher.group(i);
					LOGGER.info("Group {}: {}", i, groupI);
				}
			}
		}
		return false;
	}

	public void updateFilter() {
		for (Entry<String, RegExData> entry : forbidden.entrySet()) {
			RegExData val = entry.getValue();
			updateFilter(val);
		}
		for (Entry<String, RegExData> entry : allow.entrySet()) {
			RegExData val = entry.getValue();
			updateFilter(val);
		}
	}

	public void updateFilter(List<RegExData> filterRegExDatas) {
		allow.clear();
		forbidden.clear();
		for (RegExData regExData : filterRegExDatas) {
			updateFilter(regExData);
		}
	}

	public void updateFilter(RegExData regExData) {
		if (regExData.isEnabled()) {
			if (regExData.isDeny()) {
				forbidden.putIfAbsent(regExData.getId(), regExData);
			} else if (regExData.isAllow()) {
				allow.putIfAbsent(regExData.getId(), regExData);
			}
		} else {
			if (regExData.isDeny()) {
				forbidden.remove(regExData.getId(), regExData);
			} else if (regExData.isAllow()) {
				allow.remove(regExData.getId(), regExData);
			}
		}
		LOGGER.info("{}", regExData);
	}

}
