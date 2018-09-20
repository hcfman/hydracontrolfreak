package com.hydracontrolfreak.hcf.eventlib;

public class ConfigureWatchdogEvent extends AbstractEvent {
	
	public ConfigureWatchdogEvent() {
		eventType = EventType.EVENT_CONFIGURE;
	}

	@Override
	public String toString() {
		return "ConfigureWatchdogEvent []";
	}

}
