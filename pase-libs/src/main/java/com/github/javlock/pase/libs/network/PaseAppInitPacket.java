package com.github.javlock.pase.libs.network;

import java.util.List;

import com.github.javlock.pase.libs.api.instance.Parameter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP" })
public class PaseAppInitPacket extends Packet {

	private static final long serialVersionUID = 7281179997120429699L;

	private @Getter List<Parameter> params;

	public PaseAppInitPacket(List<Parameter> parameters) {
		if (parameters == null) {
			throw new IllegalArgumentException("parameters==null");
		}
		params = parameters;
	}
}
