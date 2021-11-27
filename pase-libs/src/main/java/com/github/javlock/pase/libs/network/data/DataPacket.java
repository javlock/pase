package com.github.javlock.pase.libs.network.data;

import java.io.Serializable;

import com.github.javlock.pase.libs.network.Packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataPacket extends Packet {
	public enum ACTIONTYPE {
		SAVE, UPDATE, FETCH, REMOVE
	}

	public enum PACKETTYPE {
		REQUEST, RESPONSE
	}

	private static final long serialVersionUID = -818741299085092754L;

	private @Getter Serializable data;
	private @Getter ACTIONTYPE action;
	private @Getter PACKETTYPE type;

	public DataPacket check() throws IllegalArgumentException {
		if (action == null) {
			throw new IllegalArgumentException("action==null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type==null");
		}
		if (data == null) {
			throw new IllegalArgumentException("data==null");
		}
		return this;
	}

	public DataPacket setAction(ACTIONTYPE type) {
		if (type == null) {
			throw new IllegalArgumentException("type==null");
		}
		action = type;
		return this;
	}

	public DataPacket setData(Serializable input) throws IllegalArgumentException {
		if (input == null) {
			throw new IllegalArgumentException("data==null");
		}
		data = input;
		return this;
	}

	public DataPacket setType(PACKETTYPE input) {
		if (input == null) {
			throw new IllegalArgumentException("input==null");
		}
		type = input;
		return this;
	}

}
