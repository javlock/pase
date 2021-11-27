package com.github.javlock.pase.libs.data.web;

import java.io.Serializable;
import java.util.Objects;

import com.github.javlock.pase.libs.utils.web.url.UrlUtils;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "urls")
public class UrlData implements Serializable {

	public enum URLTYPE {
		UKNOWN, // 0

		PAGE, FILE, // 200

		NOTGETTED, //
		OLDPROTO, // TOR v2
		//
		SOCKETTIMEOUTEXCEPTION, SOCKETEXCEPTION, SSLHANDSHAKEEXCEPTION,
		//
		NOTFOUND, // 404
		FORBIDDEN;// 403
	}

	private static final long serialVersionUID = 3287343659643626439L;

	private @Getter @Setter @DatabaseField(id = true) int hashId;

	private @Getter @DatabaseField(width = 2400) String domain;
	private @Getter @DatabaseField(width = 2400) String url;
	private @Getter @Setter @DatabaseField(width = 2400) String title;
	private @Getter @Setter @DatabaseField Long time = -1L;
	private @Getter @Setter @DatabaseField int statusCode = -1;
	private @Getter boolean builded = false;
	private @Getter @Setter @DatabaseField URLTYPE pageType = URLTYPE.UKNOWN;

	public UrlData build() {
		url = UrlUtils.getUrlWithOutSession(url);
		domain = UrlUtils.getDomainByUrl(url);

		if (domain == null) {
			throw new IllegalArgumentException(String.format("UrlData.build():%s", url));
		}
		hashId = url.hashCode();
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
		builder.append(", ");
		if (pageType != null) {
			builder.append("pageType=");
			builder.append(pageType);
		}
		builder.append("]");
		return builder.toString();
	}

}
