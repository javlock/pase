package com.github.javlock.pase.libs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

public class Balancer {
	private static final Logger LOGGER = LoggerFactory.getLogger("Balancer");
	private @Getter @Setter int objCount;
	private @Getter @Setter int objCountMax;

	private @Getter @Setter int memory;
	private @Getter @Setter int memoryMax;

	public boolean check() {
		return objCount >= objCountMax || memory >= memoryMax;
	}

	public void netxtObj() {
		if (objCount >= objCountMax) {
			System.gc();
			LOGGER.info("gc {}", System.currentTimeMillis() / 1000);
			objCount = 0;
		}
		objCount++;
	}
}
