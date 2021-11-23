package com.github.javlock.pase.jface;

public class JFace extends Thread {

	public static void main(String[] args) {
		JFace jFace = new JFace();
		jFace.init();
		jFace.start();
	}

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
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		connect();
	}
}
