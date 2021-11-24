package com.github.javlock.pase.hub.instance.service;

import com.github.javlock.pase.hub.instance.PaseHub;
import com.github.javlock.pase.hub.instance.db.DataBase;
import com.github.javlock.pase.libs.data.RegExData;
import com.github.javlock.pase.libs.network.ConfigurationUpdatePacket;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP2" })
public class ConfigurationUpdater extends Thread {
	// TODO READ FROM DB
	private static final int MAXTHREADS = 10;

	private PaseHub hub;

	public ConfigurationUpdater(PaseHub instanceHub) {
		hub = instanceHub;
	}

	@Override
	public void run() {
		do {
			DataBase db = hub.getDb();

			ConfigurationUpdatePacket configurationUpdatePacket = new ConfigurationUpdatePacket();

			for (RegExData regExData : db.getRegExDAO()) {
				configurationUpdatePacket.getFilterRegExDatas().add(regExData);
				hub.getFilterEngine().updateFilter(regExData);
			}
			configurationUpdatePacket.setThreadMax(MAXTHREADS);

			hub.broadcast(null, configurationUpdatePacket);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}

}
