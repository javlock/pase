package com.github.javlock.pase.libs.api.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.openmbean.KeyAlreadyExistsException;

import com.github.javlock.pase.libs.api.instance.Parameter.PARAMETERFIELDS;
import com.github.javlock.pase.libs.api.instance.Parameter.PaseAppType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP" })
public class PaseApp {

	private @Getter ConcurrentHashMap<String, Parameter> parametersMap = new ConcurrentHashMap<>();

	public PaseApp appType(PaseAppType type) {
		String key = PARAMETERFIELDS.APPTYPE.toString();
		if (parametersMap.containsKey(key)) {
			throw new KeyAlreadyExistsException("key VERSION exist in parametersMap");
		} else {
			Parameter parameter = new Parameter();
			parameter.setField(PARAMETERFIELDS.APPTYPE);
			parameter.setValue(type.toString());
			parametersMap.put(key, parameter);
		}
		return this;
	}

	public List<Parameter> forInitPacket() {
		ArrayList<Parameter> parametersList = new ArrayList<>();
		for (Entry<String, Parameter> entry : parametersMap.entrySet()) {
			Parameter val = entry.getValue();
			if (!val.isSecret()) {
				parametersList.add(val);
			}
		}
		return parametersList;
	}

	public void init() {
	}

	public PaseApp version(String version) {
		String key = PARAMETERFIELDS.VERSION.toString();
		if (parametersMap.containsKey(key)) {
			throw new KeyAlreadyExistsException("key VERSION exist in parametersMap");
		} else {
			Parameter parameter = new Parameter();
			parameter.setField(PARAMETERFIELDS.VERSION);
			parameter.setValue(version);
			parametersMap.put(key, parameter);
		}
		return this;
	}
}
