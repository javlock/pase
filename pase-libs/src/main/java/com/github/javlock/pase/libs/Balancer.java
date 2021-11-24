package com.github.javlock.pase.libs;

import lombok.Getter;
import lombok.Setter;

public class Balancer {
	private @Getter @Setter int objCount;
	private @Getter @Setter int objCountMax;

	private @Getter @Setter int memory;
	private @Getter @Setter int memoryMax;

	public boolean check() {
		return objCount >= objCountMax || memory >= memoryMax;
	}
}
