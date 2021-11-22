package com.github.javlock.pase.libs.utils.io;

import java.io.IOException;
import java.io.InputStream;

public class IOUtils {

	public static InputStream getFileAsIOStream(final String fileName) {
		InputStream ioStream = IOUtils.class.getClassLoader().getResourceAsStream(fileName);
		if (ioStream == null) {
			throw new IllegalArgumentException(fileName + " is not found");
		}
		return ioStream;
	}

	public static byte[] getFileFromJarAsBytes(final String fileName) throws IOException {
		return getFileAsIOStream(fileName).readAllBytes();
	}

	private IOUtils() {
	}

}
