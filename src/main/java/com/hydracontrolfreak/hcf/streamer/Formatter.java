package com.hydracontrolfreak.hcf.streamer;

import com.hydracontrolfreak.hcf.diskwatchdog.DiskWatchdogHandler;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.hcfdevice.config.DiskState;
import com.hydracontrolfreak.hcf.hcfdevice.config.Progress;
import com.hydracontrolfreak.hcf.diskwatchdog.DiskWatchdogHandler;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.hcfdevice.config.DiskState;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.config.Progress;
import org.apache.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

public class Formatter implements Runnable {
	private static final Logger logger = Logger.getLogger(Formatter.class);
	private static final Logger opLogger = Logger.getLogger("operations");
	private HcfDeviceConfig hcfConfig;
	private LinkedBlockingQueue<Progress> progressQueue;
	private DiskWatchdogHandler diskWatchdogHandler = DiskWatchdogHandler
			.getHandler();
	private FreakApi freak;

	public Formatter(FreakApi freak) {
		this.freak = freak;
	}

	private boolean deletePartitions() {
		opLogger.info("Delete existing partitions");
		progressQueue.add(new Progress(10, "Delete existing partitions", true));
		ScriptRunner scriptRunner = new ScriptRunner();

		ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
				freak.getHcfBase() + "/bin/suwrapper", freak.getHcfBase()
						+ "/bin/deletepartitions.sh", "deletepartitions.sh");

		if (scriptRunnerResult.getResult() != 0)
			return false;

		return true;
	}

	private boolean makeFilesystem() {
		opLogger.info("Create filesystem");
		progressQueue.add(new Progress(15, "Create filesystem", true));
		ScriptRunner scriptRunner = new ScriptRunner();

		ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
				freak.getHcfBase() + "/bin/suwrapper", freak.getHcfBase()
						+ "/bin/makefilesystem.sh", "makefilesystem.sh",
				freak.getHcfBase());

		if (scriptRunnerResult.getResult() != 0)
			return false;

		return true;
	}

	@Override
	public void run() {
		/* Formatting could go in here */
	}

}
