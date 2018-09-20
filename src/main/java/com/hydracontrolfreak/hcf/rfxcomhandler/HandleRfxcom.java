package com.hydracontrolfreak.hcf.rfxcomhandler;

import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.Action;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import org.apache.log4j.Logger;

public class HandleRfxcom implements Runnable {
	private static final Logger logger = Logger.getLogger(HandleRfxcom.class);
	private Event event;
	private Event originalEvent;
	private Action action;
	private HcfDeviceConfig hcfConfig;
	private RfxcomController rfxcomController;
	private FreakApi freak;

	public HandleRfxcom(FreakApi freak, RfxcomController rfxcomController,
                        Event event, Event originalEvent, Action action) {
		this.freak = freak;
		this.event = event;
		this.originalEvent = originalEvent;
		this.action = action;
	}

	@Override
	public void run() {
		hcfConfig = freak.getHcfConfig();

		if (logger.isDebugEnabled())
			logger.debug("Have the hcf config");

	}

}
