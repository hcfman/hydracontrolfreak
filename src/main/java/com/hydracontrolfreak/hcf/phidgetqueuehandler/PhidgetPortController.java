package com.hydracontrolfreak.hcf.phidgetqueuehandler;

import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.eventlib.EventType;
import com.hydracontrolfreak.hcf.eventlib.SendActionEvent;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.Action;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.config.PhidgetActionType;
import com.hydracontrolfreak.hcf.hcfdevice.config.PhidgetDevice;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.PhidgetActionImpl;
import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.eventlib.EventType;
import com.hydracontrolfreak.hcf.eventlib.SendActionEvent;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.Action;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.config.PhidgetDevice;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.PhidgetActionImpl;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import org.apache.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

public class PhidgetPortController implements Runnable {
	private static final Logger logger = Logger
			.getLogger(PhidgetPortController.class);
	private static final Logger phidgetLogger = Logger.getLogger("phidget");
	private FreakApi freak;
	private LinkedBlockingQueue<Event> eventQueue;
	private HcfDeviceConfig hcfConfig;
	private PhidgetDevice phidgetDevice;
	private InterfaceKitPhidget interfaceKitPhidget;
	private int port = 0;

	public PhidgetPortController(InterfaceKitPhidget interfaceKitPhidget,
			PhidgetDevice phidgetDevice, int port, FreakApi freak,
			LinkedBlockingQueue<Event> eventQueue) {
		this.interfaceKitPhidget = interfaceKitPhidget;
		this.phidgetDevice = phidgetDevice;
		this.port = port;
		this.freak = freak;
		this.eventQueue = eventQueue;

		hcfConfig = freak.getHcfConfig();
	}

	public PhidgetDevice getPhidget() {
		return phidgetDevice;
	}

	public int getPort() {
		return port;
	}

	private void executeAction(Action action) {
		PhidgetActionImpl phidgetActionImpl = (PhidgetActionImpl) action;

		switch (phidgetActionImpl.getPhidgetActionType()) {
		case On:
			try {
				if (logger.isDebugEnabled())
					logger.debug("Set phidget ("
							+ phidgetActionImpl.getPhidgetName() + ") port ("
							+ phidgetActionImpl.getPort() + "): on");
				interfaceKitPhidget.setOutputState(port,
						!phidgetDevice.getInitialOutputState()[port]);
				phidgetLogger.info("Phidget ["
						+ phidgetActionImpl.getPhidgetName() + "] Set port("
						+ port + ") -> On");
			} catch (PhidgetException e) {
				if (logger.isDebugEnabled())
					logger.debug("Exception setting port state true");
				e.printStackTrace();
			}
			break;
		case Off:
			try {
				if (logger.isDebugEnabled())
					logger.debug("Set phidget ("
							+ phidgetActionImpl.getPhidgetName() + ") port ("
							+ phidgetActionImpl.getPort() + "): off");
				interfaceKitPhidget.setOutputState(port,
						phidgetDevice.getInitialOutputState()[port]);
				phidgetLogger.info("Phidget ["
						+ phidgetActionImpl.getPhidgetName() + "] Set port("
						+ port + ") -> Off");
			} catch (PhidgetException e) {
				if (logger.isDebugEnabled())
					logger.debug("Exception setting port state false");
				e.printStackTrace();
			}
			break;
		case Pulse:
			if (logger.isDebugEnabled())
				logger.debug("Set phidget ("
						+ phidgetActionImpl.getPhidgetName() + ") port ("
						+ phidgetActionImpl.getPort() + "): pulse ("
						+ phidgetActionImpl.getPulseTrain() + ")");
			String trainString = phidgetActionImpl.getPulseTrain();
			String[] pulseTimeStrings = trainString.split(",");
			int[] pulseTimes = new int[pulseTimeStrings.length];
			for (int i = 0; i < pulseTimes.length; i++) {
				try {
					pulseTimes[i] = Integer.parseInt(pulseTimeStrings[i]);
				} catch (NumberFormatException e) {
					if (logger.isDebugEnabled())
						logger.debug("Invalid pulse time ("
								+ pulseTimeStrings[i] + ")");
					e.printStackTrace();
					return;
				}
			}

			boolean state = !phidgetDevice.getInitialOutputState()[port];
			for (int pulseTime : pulseTimes) {
				try {
					// if (logger.isDebugEnabled())
					// logger.debug("Setting phidget (" +
					// phidgetDevice.getSerialNumber() +
					// ") port (" + port +
					// ") state to (" + state +
					// ")");
					interfaceKitPhidget.setOutputState(port, state);
					if (logger.isDebugEnabled())
						logger.debug("Pulse port (" + port + ") state: "
								+ state);
				} catch (PhidgetException e) {
					if (logger.isDebugEnabled())
						logger.debug("Exception setting phidget ("
								+ phidgetDevice.getName() + ") port (" + port
								+ ") to state (" + state + ")");
					e.printStackTrace();
				}

				try {
					Thread.sleep(pulseTime);
				} catch (InterruptedException e) {
					if (logger.isDebugEnabled())
						logger.debug("Exception sleeping for time ("
								+ pulseTime + ")");
					e.printStackTrace();
				}
				state = !state;
			}

			// Set back to initial output state
			try {
				interfaceKitPhidget.setOutputState(port,
						phidgetDevice.getInitialOutputState()[port]);
			} catch (PhidgetException e) {
				if (logger.isDebugEnabled())
					logger.debug("Exception setting phidget ("
							+ phidgetDevice.getName() + ") port (" + port
							+ ") to state (" + state + ")");
				e.printStackTrace();
			}

			phidgetLogger.info("Phidget [" + phidgetActionImpl.getPhidgetName()
					+ "] Set port(" + port + ") -> Pulse ("
					+ phidgetActionImpl.getPulseTrain() + ")");
			break;
		}

	}

	@Override
	public void run() {
		if (logger.isDebugEnabled())
			logger.debug("Starting phidget port handler run()");

		while (true) {
			Event event = null;
			try {
				event = eventQueue.take();
				if (event.getEventType() == EventType.EVENT_SHUTDOWN)
					return;

				switch (event.getEventType()) {
				case EVENT_CONFIGURE:
					break;
				case EVENT_SHUTDOWN:
					return;
				case EVENT_ACTION:
					if (logger.isDebugEnabled())
						logger.debug("Process EVENT_ACTION for PhidgetPortController");
					if (!(event instanceof SendActionEvent)) {
						if (logger.isDebugEnabled())
							logger.debug("Wrong type of event for PhidgetPortController sending");
						break;
					}

					if (logger.isDebugEnabled())
						logger.debug("Looking good, cast action");
					SendActionEvent sendActionEvent = (SendActionEvent) event;
					Action action = sendActionEvent.getAction();

					if (!(action instanceof PhidgetActionImpl))
						break;

					if (logger.isDebugEnabled())
						logger.debug("All green for HTTP action");
					executeAction(action);
					break;
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
