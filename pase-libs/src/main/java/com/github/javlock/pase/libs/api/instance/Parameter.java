package com.github.javlock.pase.libs.api.instance;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class Parameter implements Serializable {
	public enum PARAMETERFIELDS {
		UUID, TYPE, VERSION, APPTYPE
	}

	public enum PaseAppType {
		CLIENT, HUB, SERVICE
	}

	private static final long serialVersionUID = 8062489237466957280L;

	private @Getter @Setter PARAMETERFIELDS field;
	private @Getter @Setter String value;
	private @Getter @Setter boolean secret;

}
