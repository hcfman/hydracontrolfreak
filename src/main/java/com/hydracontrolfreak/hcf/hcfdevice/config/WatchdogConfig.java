package com.hydracontrolfreak.hcf.hcfdevice.config;

import java.util.HashMap;
import java.util.Map;

public class WatchdogConfig {
	private Map<String, Watchdog> triggerMap = new HashMap<>();

	public void put(final String eventName, final Watchdog trigger) {
		triggerMap.put(eventName, trigger);
	}
	
	public Watchdog get(final String eventName) {
		return triggerMap.get(eventName);
	}
	
	public void remove(final String eventName) {
		triggerMap.remove(eventName);
	}
	
	public Map<String, Watchdog> getTriggerMap() {
		return triggerMap;
	}

	public void setTriggerMap(final Map<String, Watchdog> triggerMap) {
		this.triggerMap = triggerMap;
	}

	@Override
	public String toString() {
		return "WatchdogConfig [triggerMap=" + triggerMap + "]";
	}
}
