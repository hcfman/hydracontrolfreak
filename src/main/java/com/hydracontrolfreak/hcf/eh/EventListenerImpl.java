package com.hydracontrolfreak.hcf.eh;

import com.hydracontrolfreak.hcf.eventlib.SyntheticTriggerEvent;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import org.apache.log4j.Logger;

public class EventListenerImpl implements EventListener {
	private static final Logger logger = Logger.getLogger(EventListenerImpl.class);
	private FreakApi freak;

	public EventListenerImpl(FreakApi freak) {
		this.freak = freak;
	}

	public void onEvent(SyntheticTriggerEvent event) {
		if (logger.isDebugEnabled())
			logger.debug("Got event: " + event);

		freak.sendEvent(event);	}
}
