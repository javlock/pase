package com.github.javlock.pase.web.crawler.data;

import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

public class UrlData implements Serializable {
	public enum URLTYPE {
		PAGE, FILE
	}

	private static final long serialVersionUID = 4979287113557476414L;
	private @Getter String domain;
	private @Getter String url;
	private @Getter @Setter String title;
	private @Getter boolean builded = false;
	private @Getter @Setter URLTYPE pageType;

	public UrlData build() {
		if (domain == null) {
			throw new IllegalArgumentException(String.format("UrlData.build():%s", url));
		}
		builded = true;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UrlData other = (UrlData) obj;
		return Objects.equals(domain, other.domain) && Objects.equals(url, other.url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(domain, url);
	}

	public UrlData setDomain(String domainByUrl) {
		domain = domainByUrl;
		return this;
	}

	public UrlData setUrl(String inputUrl) {
		url = inputUrl;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UrlData [");
		if (domain != null) {
			builder.append("domain=");
			builder.append(domain);
			builder.append(", ");
		}
		if (url != null) {
			builder.append("url=");
			builder.append(url);
			builder.append(", ");
		}
		if (title != null) {
			builder.append("title=");
			builder.append(title);
			builder.append(", ");
		}
		builder.append("builded=");
		builder.append(builded);
		builder.append("]");
		return builder.toString();
	}

}
