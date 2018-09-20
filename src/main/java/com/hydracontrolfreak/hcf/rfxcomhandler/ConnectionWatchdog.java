package com.hydracontrolfreak.hcf.rfxcomhandler;

import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionWatchdog implements Runnable {
	private static final Logger logger = Logger
			.getLogger(ConnectionWatchdog.class);
	private static final Logger rfxcomLogger = Logger.getLogger("rfxcom");
	private static final Logger opLogger = Logger.getLogger("operations");
	private final int RETRY_TIME = 2000;
	private LinkedBlockingQueue<Event> watchdogQueue;
	private FileInputStream fis = null;
	private FileOutputStream fos = null;
	private Executor packetReaderExecutor = Executors.newSingleThreadExecutor();
	private FreakApi freak;
	private volatile HcfDeviceConfig hcfConfig;
	private volatile RfxcomController rfxcomController;

	public ConnectionWatchdog(RfxcomController rfxcomController,
			HcfDeviceConfig hcfConfig, FreakApi freak,
			LinkedBlockingQueue<Event> watchdogQueue) {
		logger.debug("Creating ConnectionWatchdog");
		this.rfxcomController = rfxcomController;
		this.hcfConfig = hcfConfig;
		this.freak = freak;
		this.watchdogQueue = watchdogQueue;
	}

	private void setTTYspeed() {
		logger.debug("Setting tty speed");
		ScriptRunner scriptRunner = new ScriptRunner();

		scriptRunner.spawn(freak.getHcfBase() + "/bin/setspeed.sh");
	}

	@Override
	public void run() {
		logger.debug("Starting connection watchdog");
		boolean connected = false;
		File ttyFile = new File("/dev/ttyUSB0");

		while (true) {
			boolean changed = (connected && (!ttyFile.exists()
					|| !ttyFile.canRead() || !ttyFile.canWrite()))
					|| (!connected && (ttyFile.exists() && ttyFile.canRead() && ttyFile
							.canWrite()));

			/*
			 * Connect or disconnect if connection status changed
			 */
			if (changed) {
				logger.debug("Connected status changed");
				if (!connected) {
					setTTYspeed();
					try {
						fis = new FileInputStream(ttyFile);
						fos = new FileOutputStream(ttyFile);
					} catch (FileNotFoundException e) {
						logger.error("Can't open input streams", e);
						opLogger.error("New USB device added, but I'm having trouble reading from it to establish it"
								+ " as a RFXCOM device");
					}

					if (fis != null && fos != null) {
						packetReaderExecutor.execute(new HandlePackets(
								rfxcomController, fis, fos));
					} else {
						try {
							if (fis != null)
								fis.close();
							if (fos != null)
								fos.close();

							fis = null;
							fos = null;
						} catch (IOException e) {
							logger.error("Exception closing USB streams", e);
						}
					}
					connected = true;
				} else {
					rfxcomController.setConnected(false);
					rfxcomLogger.info("RFXCOM stopped, device probably removed");
					
					try {
						/*
						 * Thread should get an exception and die after this.
						 */
						if (fis != null)
							fis.close();
						if (fos != null)
							fos.close();

						fis = null;
						fos = null;

					} catch (IOException e) {
						logger.error("Exception closing USB streams", e);
					}

					connected = false;
				}
			}

			/*
			 * Sleep then try again
			 */
			try {
				Thread.sleep(RETRY_TIME);
			} catch (InterruptedException e) {
				logger.error("Exception whilst sleeping", e);
			}
		}

	}

}
