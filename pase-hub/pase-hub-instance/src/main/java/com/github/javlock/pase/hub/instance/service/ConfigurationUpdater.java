package com.github.javlock.pase.hub.instance.service;

import com.github.javlock.pase.hub.instance.PaseHub;
import com.github.javlock.pase.hub.instance.db.DataBase;

public class ConfigurationUpdater extends Thread {
	private PaseHub hub;

	public ConfigurationUpdater(PaseHub instanceHub) {
		hub = instanceHub;
	}

	@Override
	public void run() {
		do {
			DataBase db = hub.getDb();

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}

}
