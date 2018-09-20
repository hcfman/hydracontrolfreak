package com.hydracontrolfreak.hcf.hcfdevice.config;

import java.util.Collection;

public interface Watchdog {
	Collection<String> getTriggerEventNames();

	void setTriggerEventNames(Collection<String> triggerEventNames);

	long getWithinSeconds();

	void setWithinSeconds(long withinSeconds);

	String getResult();

	void setResult(String result);
}
