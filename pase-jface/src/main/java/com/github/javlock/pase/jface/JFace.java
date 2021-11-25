package com.github.javlock.pase.jface;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JFace extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger("JFace");

	public static void main(String[] args) {
		JFace jFace = new JFace();
		jFace.init();
		jFace.start();
	}

	private JfaceService jfaceService = new JfaceService(this);

	private void connect() {
		// TODO Auto-generated method stub

	}

	private void init() {
		initService();
		initNetwork();
	}

	private void initNetwork() {
		// TODO Auto-generated method stub

	}

	private void initService() {
		jfaceService.init();
	}

	@Override
	public void run() {
		File inFile = new File("input_dir");
		if (!inFile.exists()) {
			try {
				Files.createDirectories(inFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			for (File file : inFile.listFiles()) {
				File dir = new File(file.getParentFile(), "cut");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				File newFile = new File(dir, System.currentTimeMillis() + ".png");

				jfaceService.appendFile(file, newFile);
				Optional<JFaceAnswer> answOptional = jfaceService.appendFile(file);

			}
		}
		connect();
	}

}
