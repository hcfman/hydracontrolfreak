package com.hydracontrolfreak.hcf.rfxcomhandler;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class HandlePackets implements Runnable {
	private static final Logger logger = Logger.getLogger(HandlePackets.class);
	private FileInputStream fis;
	private FileOutputStream fos;
	private RfxcomController rfxcomController;

	public HandlePackets(RfxcomController rfxcomController, FileInputStream fis, FileOutputStream fos) {
		this.rfxcomController = rfxcomController;
		this.fis = fis;
		this.fos = fos;
	}

	@Override
	public void run() {
		try {
			rfxcomController.start(fis, fos);
		} catch (FileNotFoundException e) {
			logger.error("File not found Exception starting RFXCOM controller", e);
		}
	}

}
