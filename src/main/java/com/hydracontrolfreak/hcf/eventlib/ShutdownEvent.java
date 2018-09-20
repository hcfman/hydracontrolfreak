package com.hydracontrolfreak.hcf.eventlib;

public class ShutdownEvent extends AbstractEvent {

	public ShutdownEvent() {
		eventType = EventType.EVENT_SHUTDOWN;
	}

}
