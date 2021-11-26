package com.github.javlock.pase.libs.data;

import java.io.Serializable;

public class DataObject implements DataObjectInterface, Serializable {
	private static final long serialVersionUID = -6903121402068517823L;

	@Override
	public boolean save() {
		throw new UnsupportedOperationException(String.format("for class %s", getClass()));
	}

}
