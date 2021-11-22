package com.github.javlock.pase.libs.data.web;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class UpdatedUrlData implements Serializable {
	private static final long serialVersionUID = 7211131414986490729L;
	private @Getter @Setter UrlData newData;

	private @Getter ArrayList<String> detected = new ArrayList<>();
	private @Getter ArrayList<UrlData> forbidden = new ArrayList<>();
	private @Getter ArrayList<UrlData> notFound = new ArrayList<>();

	private @Getter ArrayList<String> mailDetected = new ArrayList<>();
	private @Getter ArrayList<UrlData> fileDetected = new ArrayList<>();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UpdatedUrlData [");
		if (newData != null) {
			builder.append("newData=");
			builder.append(newData);
			builder.append(", ");
		}
		if (detected != null) {
			builder.append("detected=");
			builder.append(detected);
			builder.append(", ");
		}
		if (forbidden != null) {
			builder.append("forbidden=");
			builder.append(forbidden);
			builder.append(", ");
		}
		if (notFound != null) {
			builder.append("notFound=");
			builder.append(notFound);
			builder.append(", ");
		}
		if (mailDetected != null) {
			builder.append("mailDetected=");
			builder.append(mailDetected);
			builder.append(", ");
		}
		if (fileDetected != null) {
			builder.append("fileDetected=");
			builder.append(fileDetected);
		}
		builder.append("]");
		return builder.toString();
	}

}
