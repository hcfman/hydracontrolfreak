package com.hydracontrolfreak.hcf.streamer;

import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.hcfdevice.config.Progress;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.config.Progress;
import com.hydracontrolfreak.hcf.json.UpdateCheckJSON;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Downloader implements Runnable {
	private static final Logger opLogger = Logger.getLogger("operations");
	private static final int MAX_TRY_SECONDS = 7200;
	private HcfDeviceConfig hcfConfig;
	private LinkedBlockingQueue<Progress> progressQueue;
	private UpdateCheckJSON updateCheckJSON;
	private FreakApi freak;

	public Downloader(FreakApi freak, UpdateCheckJSON updateCheckJSON) {
		this.freak = freak;
		this.updateCheckJSON = updateCheckJSON;
	}

	private class StartUpdate implements Runnable {
		public void run() {
			synchronized (freak) {
				try {
					freak.getUpdating().set(true);
					freak.mountReadWrite();
					opLogger.info("Starting update");
					ScriptRunner scriptRunner = new ScriptRunner();

					ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
							freak.getHcfBase() + "/bin/suwrapper",
							System.getenv("HOME") + "/update/bin/update.sh",
							"update.sh", updateCheckJSON.getVersion(),
							Integer.toString(updateCheckJSON.getParts()));
				} catch (Exception e) {
					opLogger.error("Update failed: " + e.getMessage());
				} finally {
					freak.mountReadonly();
					opLogger.info("Finished downloading update");
				}
			}

		}
	}

	private void startUpdate() {
		Executor executor = Executors.newSingleThreadExecutor();
		executor.execute(new StartUpdate());
	}

	private int getApproxTotalSize() {
		File dir = new File("/home/tmp/");
		int count = 0;
		for (final File fileEntry : dir.listFiles()) {
			if (fileEntry.getName().matches("^update\\.\\d+\\.up$"))
				count++;
		}

		return count * 10 * 1024 * 1024;
	}

	@Override
	public void run() {
		opLogger.info("Started download");
		hcfConfig = freak.getHcfConfig();

		progressQueue = hcfConfig.getDownloadConfig().getProgressQueue();

		progressQueue.add(new Progress(5, "Download started", true));

		startUpdate();

		int approxTotalSize = updateCheckJSON.getParts() * 10 * 1024 * 1024;
		int loopCount = 0;
		File updateAvailableFile = new File("/home/tmp/" + "update_available");
		while (true) {
			try {
				Thread.sleep(300);
				if (updateAvailableFile.exists()) {
					progressQueue.add(new Progress(100, "Finished downloading",
							false));
					opLogger.info("Finished download");
					return;
				}

				double currentTotal = (double) getApproxTotalSize();
				int percentage = (int) (currentTotal / approxTotalSize * 90);

				if (loopCount >= MAX_TRY_SECONDS) {
					progressQueue.add(new Progress(percentage,
							"Timed exceeded during download, aborting", false));
					opLogger.info("Timed exceeded during download, aborting");
					return;
				}

				progressQueue
						.add(new Progress(percentage, "Downloading", true));
			} catch (InterruptedException e) {
				progressQueue.add(new Progress(100, "Sleep failure", false));
				opLogger.info("Sleep failure, download aborted");
				return;
			}

			loopCount++;
		}
	}

}
