package com.github.javlock.pase.web.filedownloader;

import java.util.Optional;

import com.github.javlock.pase.libs.data.web.UrlData;
import com.github.javlock.pase.web.filedownloader.storage.FDStorage;

public class Filedownloader extends Thread {

	public static void main(String[] args) {
		Filedownloader filedownloader = new Filedownloader();
		filedownloader.init();
		filedownloader.start();
	}

	private final FDStorage storage = new FDStorage();

	private void init() {
		initService();
		initNetwork();
	}

	private void initNetwork() {
		// TODO Auto-generated method stub
		// connect
	}

	private void initService() {
		// балансер

	}

	@Override
	public void run() {
		Thread.currentThread().setName("Filedownloader");
		do {
			// parseFile

			// download File
			Optional<UrlData> urlDataOptional = storage.nextUrl();

			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}

}
