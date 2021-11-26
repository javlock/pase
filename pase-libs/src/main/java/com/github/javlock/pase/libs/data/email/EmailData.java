package com.github.javlock.pase.libs.data.email;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "emails")
public class EmailData implements Serializable {
	private static final long serialVersionUID = -8247665874980668515L;

	private @Getter @Setter @DatabaseField(id = true) int hashId;
	private @Getter @DatabaseField(width = 2400) String email;

	private @Getter boolean builded = false;

	public EmailData build() {
		if (email == null) {
			throw new IllegalArgumentException(String.format("EmailData.build():%s", email));
		}
		hashId = email.hashCode();
		builded = true;
		return this;
	}
}
