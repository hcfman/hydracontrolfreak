package com.hydracontrolfreak.hcf.phidgetqueuehandler;

import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.eventlib.EventType;
import com.hydracontrolfreak.hcf.eventlib.SendActionEvent;
import com.hydracontrolfreak.hcf.eventlib.ShutdownEvent;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.Action;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.config.PhidgetDevice;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.PhidgetActionImpl;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class PhidgetQueueHandler {
	private static final Logger logger = Logger
			.getLogger(PhidgetQueueHandler.class);
	private FreakApi freak;
	private LinkedBlockingQueue<Event> eventQueue;
	private HcfDeviceConfig hcfConfig;
	private Map<Integer, LinkedBlockingQueue<Event>> phidgetQueue = new HashMap<Integer, LinkedBlockingQueue<Event>>();
	private List<Integer> phidgetList = new ArrayList<Integer>();

	public PhidgetQueueHandler(FreakApi freak,
			LinkedBlockingQueue<Event> eventQueue) {
		this.freak = freak;
		this.eventQueue = eventQueue;

		hcfConfig = freak.getHcfConfig();
	}

	private void startControllers() {
		if (logger.isDebugEnabled())
			logger.debug("Starting PhidgetQueueHandler handler");
		for (PhidgetDevice phidgetDevice : hcfConfig.getPhidgetConfig()
				.getPhidgetMap().values()) {
			int serialNum = phidgetDevice.getSerialNumber();
			phidgetQueue.put(serialNum, new LinkedBlockingQueue<Event>());

			// Save serial number so can shutdown before restarting
			phidgetList.add(serialNum);

			// Spawn the phidget controller
			PhidgetThreadFactory phidgetThreadFactory = new PhidgetThreadFactory();
			if (logger.isDebugEnabled())
				logger.debug("Start new thread");
			phidgetThreadFactory.newThread(
					new PhidgetController(phidgetDevice, freak, phidgetQueue
							.get(phidgetDevice.getSerialNumber()))).start();
		}
	}

	private void stopControllers() {
		if (logger.isDebugEnabled())
			logger.debug("Stopping all of the phidget controllers");
		for (int serialNum : phidgetList) {
			LinkedBlockingQueue<Event> queue = phidgetQueue.get(serialNum);
			if (queue == null) {
				if (logger.isDebugEnabled())
					logger.debug("Phidget (" + phidgetQueue
							+ ") has not been setup");
				break;
			}

			try {
				queue.put(new ShutdownEvent());
			} catch (InterruptedException e) {
				logger.error("Interrupted exception whilst putting ConfigureEvent on the queue: "
						+ e.getMessage());
			}
		}
	}

	public void start() {

		Thread thread = new Thread() {

			@Override
			public void run() {
				startControllers();
				Executor executor = Executors.newFixedThreadPool(1);

				while (true) {
					Event event = null;
					try {
						event = eventQueue.take();
						if (event.getEventType() == EventType.EVENT_SHUTDOWN)
							return;

						switch (event.getEventType()) {
						case EVENT_CONFIGURE:
							if (logger.isDebugEnabled())
								logger.debug("Got a configuration event");
							stopControllers();
							try {
								Thread.sleep(1000);
							} catch (Exception e) {
								logger.error("Caught exception whilst sleeping");
							}
							startControllers();
							if (logger.isDebugEnabled())
								logger.debug("Restarted controllers");
							break;
						case EVENT_SHUTDOWN:
							return;
						case EVENT_ACTION:
							if (logger.isDebugEnabled())
								logger.debug("Process EVENT_ACTION for PhidgetActionImpl");
							if (!(event instanceof SendActionEvent)) {
								if (logger.isDebugEnabled())
									logger.debug("Wrong type of event for PhidgetActionImpl sending");
								break;
							}

							if (logger.isDebugEnabled())
								logger.debug("Looking good, cast action");
							SendActionEvent sendActionEvent = (SendActionEvent) event;
							Action action = sendActionEvent.getAction();

							if (!(action instanceof PhidgetActionImpl))
								break;

							PhidgetActionImpl phidgetActionImpl = (PhidgetActionImpl) action;
							PhidgetDevice selectedPhidget = hcfConfig
									.getPhidgetConfig().getPhidgetMap()
									.get(phidgetActionImpl.getPhidgetName());

							if (selectedPhidget == null) {
								if (logger.isDebugEnabled())
									logger.debug("Invalid phidget name");
								break;
							}

							LinkedBlockingQueue<Event> queue = phidgetQueue
									.get(selectedPhidget.getSerialNumber());
							if (queue == null) {
								if (logger.isDebugEnabled())
									logger.debug("Phidget (" + phidgetQueue
											+ ") has not been setup");
								break;
							}

							queue.put(event);

							if (logger.isDebugEnabled())
								logger.debug("All green for HTTP action");
							// executor.execute(new HandleHttp(event,
							// sendActionEvent.getOriginalEvent(), action));
							break;
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		thread.setName("phidget-queue-handler");
		thread.start();

	}

}
