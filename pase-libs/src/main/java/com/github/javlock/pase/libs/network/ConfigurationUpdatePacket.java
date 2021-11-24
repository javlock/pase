package com.github.javlock.pase.libs.network;

import java.util.ArrayList;

import com.github.javlock.pase.libs.data.RegExData;

import lombok.Getter;
import lombok.Setter;

public class ConfigurationUpdatePacket extends Packet {
	private static final long serialVersionUID = 2705928116898518428L;

	private final @Getter ArrayList<RegExData> filterRegExDatas = new ArrayList<>();
	private @Getter @Setter Integer threadMax;
}
