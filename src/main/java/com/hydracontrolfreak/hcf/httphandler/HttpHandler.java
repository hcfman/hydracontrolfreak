package com.hydracontrolfreak.hcf.httphandler;

import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.eventlib.EventType;
import com.hydracontrolfreak.hcf.eventlib.SendActionEvent;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.Action;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.HttpActionImpl;
import org.apache.log4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class HttpHandler {
	private static final Logger logger = Logger.getLogger(HttpHandler.class);
	FreakApi freak;
	LinkedBlockingQueue<Event> eventQueue;
	HcfDeviceConfig hcfConfig;

	public HttpHandler(FreakApi freak, LinkedBlockingQueue<Event> eventQueue) {
		this.freak = freak;
		this.eventQueue = eventQueue;

		hcfConfig = freak.getHcfConfig();

		if (logger.isDebugEnabled())
			logger.debug("Have setup the http handler");
	}

	public void start() {

		Thread thread = new Thread() {

			@Override
			public void run() {
				if (logger.isDebugEnabled())
					logger.debug("Starting http handler");
				int numThreads = hcfConfig.getSettingsConfig().getHttpThreads();

				Executor executor = Executors.newFixedThreadPool(numThreads);

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

							if (!(action instanceof HttpActionImpl))
								break;

							if (logger.isDebugEnabled())
								logger.debug("All green for HTTP action");
							executor.execute(new HandleHttp(freak, event,
									sendActionEvent.getOriginalEvent(), action));
							break;
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		thread.setName("http-handler");
		thread.start();

	}

}
