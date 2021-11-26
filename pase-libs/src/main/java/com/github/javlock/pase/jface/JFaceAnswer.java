package com.github.javlock.pase.jface;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class JFaceAnswer {

	public static class RectObj {

		@AllArgsConstructor
		public static class FacePoint {
			private @Getter @Setter int x1;
			private @Getter @Setter int y1;
			private @Getter @Setter int x2;
			private @Getter @Setter int y2;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				FacePoint other = (FacePoint) obj;
				return x1 == other.x1 && x2 == other.x2 && y1 == other.y1 && y2 == other.y2;
			}

			@Override
			public int hashCode() {
				return Objects.hash(x1, x2, y1, y2);
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append("FacePoint [x1=");
				builder.append(x1);
				builder.append(", y1=");
				builder.append(y1);
				builder.append(", x2=");
				builder.append(x2);
				builder.append(", y2=");
				builder.append(y2);
				builder.append("]");
				return builder.toString();
			}

		}

		private @Getter @Setter String sha256;
		private @Getter CopyOnWriteArrayList<FacePoint> faces = new CopyOnWriteArrayList<>();

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			RectObj other = (RectObj) obj;
			return Objects.equals(sha256, other.sha256);
		}

		@Override
		public int hashCode() {
			return Objects.hash(sha256);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("RectObj [");
			if (sha256 != null) {
				builder.append("sha256=");
				builder.append(sha256);
				builder.append(", ");
			}
			if (faces != null) {
				builder.append("faces=");
				builder.append(faces);
			}
			builder.append("]");
			return builder.toString();
		}
	}

	private @Getter @Setter String sha256;
	private @Getter ConcurrentHashMap<String, RectObj> rects = new ConcurrentHashMap<>();

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JFaceAnswer other = (JFaceAnswer) obj;
		return Objects.equals(sha256, other.sha256);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sha256);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JFaceAnswer [");
		if (sha256 != null) {
			builder.append("sha256=");
			builder.append(sha256);
			builder.append(", ");
		}
		if (rects != null) {
			builder.append("rects=");
			builder.append(rects);
		}
		builder.append("]");
		return builder.toString();
	}

}
