package com.hydracontrolfreak.hcf.phidgetqueuehandler;

import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.eventlib.PhidgetTriggerEvent;
import com.hydracontrolfreak.hcf.eventlib.SendActionEvent;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.Action;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.config.PhidgetDevice;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.PhidgetActionImpl;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.PhidgetConstants;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class PhidgetController implements Runnable {
	private static final Logger logger = Logger
			.getLogger(PhidgetController.class);
	private static final Logger opLogger = Logger.getLogger("operations");
	private static final Logger phidgetLogger = Logger.getLogger("phidget");
	private FreakApi freak;
	private LinkedBlockingQueue<Event> eventQueue;
	private Map<Integer, LinkedBlockingQueue<Event>> phidgetPortQueue = new HashMap<Integer, LinkedBlockingQueue<Event>>();
	private HcfDeviceConfig hcfConfig;
	private PhidgetDevice phidgetDevice;
	private InterfaceKitPhidget interfaceKitPhidget;
	private Map<Integer, String> returnMap = new ConcurrentHashMap<Integer, String>();
	private Map<Integer, PhidgetPortController> portControllerMap = new ConcurrentHashMap<Integer, PhidgetPortController>();
	private int[] eventCount = new int[PhidgetConstants.PHIDGET_PORT_SIZE];
	private AttachListener attachListener;
	private DetachListener detachListener;
	private ErrorListener errorListener;
	private InputChangeListener inputChangeListener;

	public PhidgetController(PhidgetDevice phidget, FreakApi freak,
			LinkedBlockingQueue<Event> eventQueue) {
		this.phidgetDevice = phidget;
		this.freak = freak;
		this.eventQueue = eventQueue;

		hcfConfig = freak.getHcfConfig();

		try {
			interfaceKitPhidget = new InterfaceKitPhidget();
		} catch (PhidgetException e) {
			e.printStackTrace();
		}

		if (logger.isDebugEnabled())
			logger.debug("Have setup the PhidgetController handler");
	}

	public PhidgetDevice getPhidgetDevice() {
		return phidgetDevice;
	}

	private void initialise() {
		if (logger.isDebugEnabled())
			logger.debug("phidget: " + phidgetDevice);
		for (int i = 0; i < phidgetDevice.getPortSize(); i++) {
			if (logger.isDebugEnabled())
				logger.debug("i: " + i);
			try {
				interfaceKitPhidget.setOutputState(i,
						phidgetDevice.getInitialOutputState()[i]);
				phidgetLogger.info("Phidget ["
						+ phidgetDevice.getSerialNumber()
						+ "] Set port ("
						+ i
						+ ") -> "
						+ (phidgetDevice.getInitialOutputState()[i] ? "On"
								: "Off"));

			} catch (PhidgetException e) {
				e.printStackTrace();
			}

		}

	}

	public void mapEvent(int port, String result) {
		returnMap.put(port, result);
	}

	private void startPhidgetListeners() {
		try {
			interfaceKitPhidget
					.addAttachListener(attachListener = new AttachListener() {
						public void attached(AttachEvent ae) {
							try {
								if (logger.isDebugEnabled())
									logger.debug("attachment of "
											+ ae.getSource().getSerialNumber());
								phidgetDevice.setConnected(true);
								initialise();
								opLogger.info("Phidget ["
										+ ae.getSource().getSerialNumber()
										+ "] connected");
								phidgetLogger.info("Phidget ["
										+ ae.getSource().getSerialNumber()
										+ "] connected");
							} catch (PhidgetException e) {
								phidgetLogger
										.error("Error attaching to Phidget: "
												+ e.getMessage());
								e.printStackTrace();
							}
						}
					});

			interfaceKitPhidget
					.addDetachListener(detachListener = new DetachListener() {
						public void detached(DetachEvent ae) {
							if (logger.isDebugEnabled())
								logger.debug("detachment of " + ae);
							phidgetDevice.setConnected(false);
							try {
								opLogger.info("Phidget ["
										+ ae.getSource().getSerialNumber()
										+ "] disconnected");
								phidgetLogger.info("Phidget ["
										+ ae.getSource().getSerialNumber()
										+ "] disconnected");
							} catch (PhidgetException e) {
								phidgetLogger
										.error("Error detaching from Phidget: "
												+ e.getMessage());
								e.printStackTrace();
							}
						}
					});

			interfaceKitPhidget
					.addErrorListener(errorListener = new ErrorListener() {
						public void error(ErrorEvent ee) {
							if (logger.isDebugEnabled())
								logger.debug("error event for " + ee);
						}
					});

			interfaceKitPhidget
					.addInputChangeListener(inputChangeListener = new InputChangeListener() {

						@Override
						public void inputChanged(InputChangeEvent ae) {
							if (logger.isDebugEnabled())
								logger.debug("Input changed for: "
										+ ae.getIndex() + " to "
										+ ae.getState());
							eventCount[ae.getIndex()]++;
							// The first one is initialization
							if (eventCount[ae.getIndex()] == 1)
								return;

							String eventName = null;
							if (logger.isDebugEnabled())
								logger.debug("phidgetDevice: "
										+ phidgetDevice.getName());
							if (phidgetDevice.getInitialInputState()[ae
									.getIndex()] ^ ae.getState()) {
								if (logger.isDebugEnabled())
									logger.debug("State: true");
								eventName = phidgetDevice
										.getOnTriggerEventNames()[ae.getIndex()];
							} else {
								if (logger.isDebugEnabled())
									logger.debug("State: false");
								eventName = phidgetDevice
										.getOffTriggerEventNames()[ae
										.getIndex()];
							}

							if (eventName != null) {
								if (logger.isDebugEnabled())
									logger.debug("Callback on phidget state change "
											+ phidgetDevice.getName()
											+ ": "
											+ ae.getIndex()
											+ " with eventName ("
											+ eventName
											+ ") => ");
								freak.sendEvent(new PhidgetTriggerEvent(
										eventName, System.currentTimeMillis()));
							} else {
								if (logger.isDebugEnabled())
									logger.debug("eventName is null, index: "
											+ ae.getIndex());
							}

							try {
								phidgetLogger.info("Phidget["
										+ ae.getSource().getSerialNumber()
										+ "] Port ("
										+ ae.getIndex()
										+ ") <- "
										+ (phidgetDevice.getInitialInputState()[ae
												.getIndex()] ^ ae.getState() ? "On"
												: "Off"));
							} catch (PhidgetException e) {
								phidgetLogger
										.error("Error writing Phidget log: "
												+ e.getMessage());
								e.printStackTrace();
							}

						}
					});
			if (logger.isDebugEnabled())
				logger.debug("Want to open phidget: "
						+ phidgetDevice.getSerialNumber());
			interfaceKitPhidget.open(phidgetDevice.getSerialNumber());
		} catch (PhidgetException e) {
			phidgetLogger.error("Phidget error: " + e.getMessage());
			e.printStackTrace();
		}

	}

	private void startPhidgetPortHandlers() {
		for (int i = 0; i < PhidgetConstants.PHIDGET_PORT_SIZE; i++) {
			if (logger.isDebugEnabled())
				logger.debug("Setting phidgetPortQueue for phidget with serial number ("
						+ i + ")");
			phidgetPortQueue.put(i, new LinkedBlockingQueue<Event>());

			PhidgetPortThreadFactory phidgetPortThreadFactory = new PhidgetPortThreadFactory();
			if (logger.isDebugEnabled())
				logger.debug("Start new thread");
			PhidgetPortController portController = new PhidgetPortController(
					interfaceKitPhidget, phidgetDevice, i, freak,
					phidgetPortQueue.get(i));
			portControllerMap.put(i, portController);
			phidgetPortThreadFactory.newThread(portController).start();
		}

	}

	private void start() {
		if (logger.isDebugEnabled())
			logger.debug("Try starting phidget start(): "
					+ phidgetDevice.getSerialNumber());

		// Start the listeners. These handle input events from the phidget
		startPhidgetListeners();
		startPhidgetPortHandlers();
	}

	private void stop() {
		phidgetDevice.setConnected(false);
		opLogger.info("Stopping phidget controller");
		interfaceKitPhidget.removeErrorListener(errorListener);
		if (logger.isDebugEnabled())
			logger.debug("Removed errorListener");
		interfaceKitPhidget.removeDetachListener(detachListener);
		if (logger.isDebugEnabled())
			logger.debug("Removed detachListener");
		interfaceKitPhidget.removeAttachListener(attachListener);
		if (logger.isDebugEnabled())
			logger.debug("Removed attachListener");
		interfaceKitPhidget.removeInputChangeListener(inputChangeListener);
		if (logger.isDebugEnabled())
			logger.debug("Removed inputChangeListener");
		try {
			if (logger.isDebugEnabled())
				logger.debug("Closing interfaceKitPhidget");
			interfaceKitPhidget.close();
			if (logger.isDebugEnabled())
				logger.debug("Closed interfaceKitPhidget");
		} catch (PhidgetException e) {
			logger.error("Exception thrown whilst closing interfaceKitPhidget: "
					+ e.getMessage());
		}
	}

	@Override
	public void run() {
		if (logger.isDebugEnabled())
			logger.debug("Starting phidget handler run()");
		start();
		if (logger.isDebugEnabled())
			logger.debug("PhidgetQueueHandler Started");

		while (true) {
			Event event = null;
			try {
				if (logger.isDebugEnabled())
					logger.debug("Try and take event from the phidget controller queue");
				event = eventQueue.take();
				if (logger.isDebugEnabled())
					logger.debug("Taken event from the phidget queue");

				switch (event.getEventType()) {
				case EVENT_CONFIGURE:
					break;
				case EVENT_SHUTDOWN:
					stop();
					return;
				case EVENT_ACTION:
					if (logger.isDebugEnabled())
						logger.debug("Process EVENT_ACTION for HTTP");
					if (!(event instanceof SendActionEvent)) {
						if (logger.isDebugEnabled())
							logger.debug("Wrong type of event for HTTP sending");
						break;
					}

					if (logger.isDebugEnabled())
						logger.debug("Looking good, cast action");
					SendActionEvent sendActionEvent = (SendActionEvent) event;
					Action action = sendActionEvent.getAction();

					if (!(action instanceof PhidgetActionImpl))
						break;

					PhidgetActionImpl phidgetActionImpl = (PhidgetActionImpl) action;
					Integer port = phidgetActionImpl.getPort();
					if (port >= PhidgetConstants.PHIDGET_PORT_SIZE) {
						if (logger.isDebugEnabled())
							logger.debug("Invalid port specified (" + port
									+ ")");
						break;
					}

					phidgetPortQueue.get(port).add(event);

					if (logger.isDebugEnabled())
						logger.debug("All green for HTTP action");
					break;
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
