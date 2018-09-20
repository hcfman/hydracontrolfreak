package com.hydracontrolfreak.hcf.dvrhandler;

import com.hydracontrolfreak.hcf.dvr.Defaults;
import com.hydracontrolfreak.hcf.eventlib.*;
import com.hydracontrolfreak.hcf.hcfdevice.config.*;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.VideoActionImpl;
import com.hydracontrolfreak.hcf.webmEncoder.EncoderImpl;
import com.hydracontrolfreak.hcf.dvr.Defaults;
import com.hydracontrolfreak.hcf.dvr.VideoReader;
import com.hydracontrolfreak.hcf.eventlib.*;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.*;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.VideoActionImpl;
import com.hydracontrolfreak.hcf.webmEncoder.Encoder;
import com.hydracontrolfreak.hcf.webmEncoder.EncoderImpl;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DvrHandler {
	private static final Logger logger = Logger.getLogger(DvrHandler.class);
	private FreakApi freak;
	private LinkedBlockingQueue<Event> eventQueue;
	private Boolean ready = false;
	private ConcurrentHashMap<Integer, LinkedBlockingQueue<Event>> cameraEventQueues;
	private Defaults videoDefaults = new Defaults();
	private HcfDeviceConfig hcfConfig;
	private Encoder encoder = null;

	public DvrHandler(FreakApi freak, LinkedBlockingQueue<Event> eventQueue) {
		this.freak = freak;
		this.eventQueue = eventQueue;
	}

	public final void start() {
		logger.info("DVR HANDLER STARTUP");

		handle();
	}

	// Actually spawn the readers, this can be called after a reconfigure event
	synchronized private void spawnReaders() {
		if (logger.isDebugEnabled())
			logger.debug("Spawning videoReaders");
		Map<Integer, CameraDevice> cameraDevices = hcfConfig.getCameraConfig()
				.getCameraDevices();
		SortedSet<Integer> cameraOrder = hcfConfig.getCameraConfig()
				.getCameraOrder();

		// A new set of queues
		cameraEventQueues = new ConcurrentHashMap<Integer, LinkedBlockingQueue<Event>>();

		Map<Integer, VideoReader> videoReaders = new HashMap<Integer, VideoReader>();
		for (int camera : cameraOrder) {
			if (logger.isDebugEnabled())
				logger.debug("Spawn videoReader: " + camera);
			cameraEventQueues.put(camera, new LinkedBlockingQueue<Event>());

			VideoReader videoReader = new VideoReader(
					cameraDevices.get(camera), hcfConfig.getSettingsConfig()
							.getConnectTimeout(), freak, encoder,
					cameraDevices.get(camera), cameraEventQueues.get(camera));
			videoReaders.put(camera, videoReader);
			videoReader.start();
		}
	}

	private void startVideoRunners() {
		hcfConfig = freak.getHcfConfig();

		try {
			Integer numThreads = 1;
			SettingsConfig settings;
			settings = hcfConfig.getSettingsConfig();
			if (settings != null) {
				numThreads = settings.getEncoderThreads();
				if (numThreads == null || numThreads < 0)
					numThreads = videoDefaults.getNumThreads();

			}
			if (logger.isDebugEnabled())
				logger.debug("Setting " + numThreads + " encoder threads");
			encoder = new EncoderImpl(freak.getHcfBase() + "/disk/hcf/",
					numThreads);
		} catch (IOException e) {
			logger.error("Failed to create encoder");
			e.printStackTrace();
		}
		encoder.start();

		spawnReaders();
	}

	private void handle() {
		startVideoRunners();

		Thread thread = new Thread() {

			@Override
			public void run() {
				synchronized (ready) {
					ready = true;
				}

				while (true) {
					Event event = null;
					try {
						event = eventQueue.take();
						if (logger.isDebugEnabled())
							logger.debug("dvr-handler event: " + event);
						if (event.getEventType() == EventType.EVENT_SHUTDOWN)
							return;

						switch (event.getEventType()) {
						case EVENT_CONFIGURE:
							if (logger.isDebugEnabled())
								logger.debug("Dvr CONFIGURE event");
							if (logger.isDebugEnabled())
								logger.debug("cameraEventQueues: "
										+ cameraEventQueues);
							for (int cam : cameraEventQueues.keySet()) {
								if (logger.isDebugEnabled())
									logger.debug("Stop video for " + cam);
								LinkedBlockingQueue<Event> queue = cameraEventQueues
										.get(cam);
								if (queue != null) {
									if (logger.isDebugEnabled())
										logger.debug("Stopping camera(" + cam
												+ ")");
									if (logger.isDebugEnabled())
										logger.debug("Sending down queue: "
												+ (Object) queue);
									queue.add(new ShutdownEvent());
									cameraEventQueues.remove(cam);
								} else {
									if (logger.isDebugEnabled())
										logger.debug("There was no queue for cam: "
												+ cam);
								}

							}

							// Wait till it settles
							try {
								Thread.sleep(5000);
							} catch (Exception e) {
								e.printStackTrace();
							}

							spawnReaders();
							break;
						case EVENT_SHUTDOWN:
							if (logger.isDebugEnabled())
								logger.debug("Shutting video down...");

							for (int cam : cameraEventQueues.keySet()) {
								if (logger.isDebugEnabled())
									logger.debug("Switch video for " + cam);
								LinkedBlockingQueue<Event> queue = cameraEventQueues
										.get(cam);
								if (queue != null) {
									if (logger.isDebugEnabled())
										logger.debug("Stopping camera(" + cam
												+ ")");
									queue.add(new ShutdownEvent());
									cameraEventQueues.remove(cam);
								}

							}

							// Wait till it settles
							try {
								Thread.sleep(5000);
							} catch (Exception e) {
								e.printStackTrace();
							}

							return;
						case EVENT_ACTION:
							if (logger.isDebugEnabled())
								logger.debug("Process EVENT_ACTION for Video Trigger");
							if (!(event instanceof SendActionEvent)) {
								if (logger.isDebugEnabled())
									logger.debug("Wrong type of event for Video Trigerring sending");
								break;
							}

							if (logger.isDebugEnabled())
								logger.debug("Looking good, cast action");
							SendActionEvent sendActionEvent = (SendActionEvent) event;
							Action action = sendActionEvent.getAction();

							if (!(action instanceof VideoActionImpl))
								break;

							// Don't write if the disk isn't setup
							if (!(hcfConfig.getDiskConfig().getDiskState() == DiskState.ALL_GOOD))
								break;

							VideoActionImpl videoActionImpl = (VideoActionImpl) action;
							for (int cam : videoActionImpl.getCameraSet()) {
								if (logger.isDebugEnabled())
									logger.debug("Switch video for " + cam);
								LinkedBlockingQueue<Event> queue = cameraEventQueues
										.get(cam);
								if (queue != null) {
									if (logger.isDebugEnabled())
										logger.debug("Camera is active, sending trigger: "
												+ videoActionImpl
														.getDescription());
									queue.add(new VideoTriggerEvent(cam,
											videoActionImpl.getDescription(),
											sendActionEvent.getEventTime()));
								}
							}

							break;

						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (logger.isDebugEnabled())
						logger.debug("Got event: " + event);
				}

			}
		};

		thread.setName("dvr-handler");
		thread.start();
	}

	public boolean isReady() {
		boolean result;
		synchronized (ready) {
			result = ready;
		}
		return result;
	}

}
