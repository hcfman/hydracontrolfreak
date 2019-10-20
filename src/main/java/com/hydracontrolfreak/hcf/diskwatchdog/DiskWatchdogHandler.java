package com.hydracontrolfreak.hcf.diskwatchdog;

import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.hcfdevice.config.DiskState;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import org.apache.log4j.Logger;

import java.io.File;

public class DiskWatchdogHandler {
	private static final Logger logger = Logger
			.getLogger(DiskWatchdogHandler.class);
	private static final Logger opLogger = Logger.getLogger("operations");
	private static final long MOUNT_RETRY_TIME = 10000;
	private volatile boolean formatting = false;
	private HcfDeviceConfig hcfConfig;
	private static DiskWatchdogHandler handler;
	private volatile boolean mounted = false;
	private volatile boolean dontMount = false;
	private volatile boolean diskAvailable = false;
	private DiskState oldState;
	private DiskState newState;
	private FreakApi freak;

	public DiskWatchdogHandler() {
		handler = this;
	}

	public boolean isMounted() {
		return mounted;
	}

	public void setFormatting(boolean formatting) {
		this.formatting = formatting;
		if (formatting) {
			hcfConfig.getDiskConfig().setDiskState(DiskState.FORMATTING);
			hcfConfig.getDiskConfig().setLastMessage("Formatting");
			newState = DiskState.FORMATTING;
		} else {
			hcfConfig.getDiskConfig().setDiskState(DiskState.UN_INITIALISED);
			hcfConfig.getDiskConfig().setLastMessage("Finished formatting");
			newState = oldState = DiskState.UN_INITIALISED;
		}
	}

	public static DiskWatchdogHandler getHandler() {
		return handler;
	}

	public synchronized boolean unmount() {
		ScriptRunner scriptRunner = new ScriptRunner();
		ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
				freak.getHcfBase() + "/bin/suwrapper", freak.getHcfBase()
						+ "/bin/unmountpartition.sh", "unmountpartition.sh",
				freak.getHcfBase() + "/disk");

		boolean result = scriptRunnerResult.getResult() == 0;
		setMounted(false);
		opLogger.info("Unmounting disk");
		return result;
	}

	public final void start(FreakApi freak) {
		this.freak = freak;
		logger.info("DVR HANDLER STARTUP");

		handle();
	}

	private void setMounted(boolean mounted) {
		this.mounted = mounted;
	}

	private boolean isDontMount() {
		return dontMount;
	}

	private void setDontMount(boolean dontMount) {
		this.dontMount = dontMount;
	}

	private void setDiskAvailable(boolean diskAvailable) {
		this.diskAvailable = diskAvailable;
	}

	private boolean isFormatting() {
		return formatting;
	}

	private boolean couldMount() {
		// Don't mount when formatting
		if (isFormatting())
			return false;

		ScriptRunner scriptRunner = new ScriptRunner();

		ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
				freak.getHcfBase() + "/bin/suwrapper", freak.getHcfBase()
						+ "/bin/mountpartition.sh", "mountpartition.sh",
				freak.getHcfBase() + "/disk");

		if (scriptRunnerResult.getResult() == 0) {
			opLogger.info("Disk mounted");
			setMounted(true);
			return true;
		} else {
			setMounted(false);
			return false;
		}
	}

	private void handle() {
		hcfConfig = freak.getHcfConfig();

		Thread thread = new Thread() {

			@Override
			public void run() {
				long lastMountAttempt = 0;
				File dvrImagesFile = new File(freak.getHcfBase()
						+ "/disk/hcf/dvr_images");
				File eventsFile = new File(freak.getHcfBase()
						+ "/disk/hcf/events");
				File tmpImagesFile = new File(freak.getHcfBase()
						+ "/disk/hcf/tmp_images");

				oldState = DiskState.UN_INITIALISED;
				while (true) {
					newState = oldState;

					setDiskAvailable(true);

					if (!isMounted() && !isDontMount()) {
						if (System.currentTimeMillis()
								- lastMountAttempt >= MOUNT_RETRY_TIME) {
							lastMountAttempt = System
									.currentTimeMillis();
							if (!couldMount()) {
								if (!isFormatting()) {
									opLogger.info("Failed to mount drive");
									hcfConfig
											.getDiskConfig()
											.setLastMessage(
													"Failed to mount disk, try formatting");
								}
							} else {
								if (!dvrImagesFile.isDirectory()
										|| !dvrImagesFile.canWrite()
										|| !dvrImagesFile.canRead()
										|| !eventsFile.isDirectory()
										|| !eventsFile.canRead()
										|| !eventsFile.canWrite()
										|| !tmpImagesFile.isDirectory()
										|| !tmpImagesFile.canRead()
										|| !tmpImagesFile.canWrite()) {
									newState = DiskState.PARTITION_MOUNTED_NO_STRUCTURE;
									opLogger.info("Disk was mounted but it is not correctly formatted. Please format the disk.");
									setDontMount(true);
									unmount();
									hcfConfig
											.getDiskConfig()
											.setLastMessage(
													"Drive was mountable, but did not contain a valid structure, try formatting");
								} else {
									newState = DiskState.ALL_GOOD;
									opLogger.info("Disk was mounted and is correctly formatted");
									hcfConfig
											.getDiskConfig()
											.setLastMessage(
													"Drive mounted successfully");
								}
							}
						}
					}

					hcfConfig.getDiskConfig().setDiskState(newState);

					oldState = newState;

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						if (logger.isDebugEnabled())
							if (logger.isDebugEnabled())
								logger.debug("Interrupted sleep in disk watchdog");
					}
				}
			}

		};

		thread.setName("disk-watchdog");
		thread.start();
	}

}
