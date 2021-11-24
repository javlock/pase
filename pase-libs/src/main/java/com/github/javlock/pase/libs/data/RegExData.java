package com.github.javlock.pase.libs.data;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "urlsRegExs")
public class RegExData implements Serializable {
	private static final long serialVersionUID = 887113820774813646L;

	private @Getter @Setter @DatabaseField(id = true) String id;

	private @Getter @DatabaseField(width = 2400) String regEx;

	private @Getter @DatabaseField boolean allow;
	private @Getter @DatabaseField boolean deny;
	private @Getter @DatabaseField boolean enabled;

	public RegExData build() {
		if (regEx == null) {
			throw new NullPointerException("RegExData.regEx==null");
		}
		if (regEx.trim().isEmpty()) {
			throw new IllegalArgumentException("RegExData.regEx.trim().isEmpty()");
		}
		id = UUID.nameUUIDFromBytes(regEx.getBytes(StandardCharsets.UTF_8)).toString();
		return this;
	}

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
		RegExData other = (RegExData) obj;
		return Objects.equals(id, other.id) && Objects.equals(regEx, other.regEx);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, regEx);
	}

	public RegExData setAllow(boolean b) {
		allow = b;
		return this;
	}

	public RegExData setDeny(boolean b) {
		deny = b;
		return this;

	}

	public RegExData setEnabled(boolean b) {
		enabled = b;
		return this;
	}

	public RegExData setRegEx(String string) {
		regEx = string;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RegExData [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (regEx != null) {
			builder.append("regEx=");
			builder.append(regEx);
			builder.append(", ");
		}
		builder.append("enabled=");
		builder.append(enabled);
		builder.append(", allow=");
		builder.append(allow);
		builder.append(", deny=");
		builder.append(deny);
		builder.append("]");
		return builder.toString();
	}
}
