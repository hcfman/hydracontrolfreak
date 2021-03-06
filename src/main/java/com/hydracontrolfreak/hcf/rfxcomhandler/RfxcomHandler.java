package com.hydracontrolfreak.hcf.rfxcomhandler;

import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.eventlib.EventType;
import com.hydracontrolfreak.hcf.eventlib.SendActionEvent;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.Action;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.RfxcomActionImpl;
import com.hydracontrolfreak.hcf.rfxcomlib.RfxcomCommand;
import com.hydracontrolfreak.hcf.rfxcomlib.RfxcomType;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class RfxcomHandler {
	private static final Logger logger = Logger.getLogger(RfxcomHandler.class);
	private static final Logger rfxcomLogger = Logger.getLogger("rfxcom");
	private static final Logger oplogger = Logger.getLogger("operations");
	private FreakApi freak;
	private LinkedBlockingQueue<Event> rfxcomEventQueue;
	private LinkedBlockingQueue<Event> watchdogQueue = new LinkedBlockingQueue<Event>();
	private HcfDeviceConfig hcfConfig;
	private volatile RfxcomController rfxcomController;

	private Executor watchdogExecutor;

	public RfxcomHandler(FreakApi freak,
			LinkedBlockingQueue<Event> rfxcomEventQueue) {
		this.freak = freak;
		this.rfxcomEventQueue = rfxcomEventQueue;

		hcfConfig = freak.getHcfConfig();

		rfxcomController = new RfxcomController(freak);
	}

	public RfxcomController getRfxcomController() {
		return rfxcomController;
	}

	private void initialStartup() {
		watchdogExecutor = Executors.newSingleThreadExecutor();
		watchdogExecutor.execute(new ConnectionWatchdog(rfxcomController,
				hcfConfig, freak, watchdogQueue));

		// Initial subscribe
		resubscribe();
	}

	void resubscribe() {
		rfxcomController.clearAllSubscriptions();
		for (RfxcomCommand command : hcfConfig.getRfxcomConfig().getCommands()
				.values()) {
			if (command.getRfxcomType() == RfxcomType.GENERIC_OUTPUT)
				continue;

			rfxcomController.subscribe(new RfxComListenerImpl(command, freak));
		}

	}

	public void start() {

		Thread thread = new Thread() {

			@Override
			public void run() {
				if (logger.isDebugEnabled())
					logger.debug("Starting http handler");

				initialStartup();

				while (true) {
					Event event = null;
					try {
						event = rfxcomEventQueue.take();
						if (event.getEventType() == EventType.EVENT_SHUTDOWN)
							return;

						switch (event.getEventType()) {
						case EVENT_CONFIGURE:
							rfxcomLogger.info("RFXCOM resubscribing");
							oplogger.info("RFXCOM resubscribing");
							resubscribe();
							break;
						case EVENT_SHUTDOWN:
							return;
						case EVENT_ACTION:
							if (logger.isDebugEnabled())
								logger.debug("Process EVENT_ACTION for RFXCOM");
							if (!(event instanceof SendActionEvent)) {
								if (logger.isDebugEnabled())
									logger.debug("Wrong type of event for RFXCOM sending");
								break;
							}

							if (logger.isDebugEnabled())
								logger.debug("Looking good, cast action");

							SendActionEvent sendActionEvent = (SendActionEvent) event;
							Action action = sendActionEvent.getAction();

							if (!(action instanceof RfxcomActionImpl))
								break;

							if (logger.isDebugEnabled())
								logger.debug("All green for RFXCOM action");

							RfxcomActionImpl rfxcomActionImpl = (RfxcomActionImpl) action;

							String rfxcomName = rfxcomActionImpl
									.getRfxcomName();
							RfxcomCommand rfxcomCommand = hcfConfig
									.getRfxcomConfig().getCommands()
									.get(rfxcomName);

							if (rfxcomCommand != null) {
								int[] intArray = rfxcomCommand
										.getPacketValues1();
								logger.debug("Wanting to send an RFXCOM command: "
										+ Arrays.toString(intArray));
								rfxcomController.sendRfxcomCommand(intArray);
							} else {
								logger.info("Tried to execute RFXCOM command with name ("
										+ rfxcomName
										+ ") but it has not yet been configured. Try adding the command in the RFXCOM section");
							}

							break;
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		thread.setName("rfxcom-handler");
		thread.start();

	}
}
