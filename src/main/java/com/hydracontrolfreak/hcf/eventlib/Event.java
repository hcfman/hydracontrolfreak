package com.hydracontrolfreak.hcf.eventlib;

public interface Event {
	public EventType getEventType();
	public long getEventTime();
	public boolean isGuest();
}
