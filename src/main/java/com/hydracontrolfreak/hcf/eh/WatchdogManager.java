package com.hydracontrolfreak.hcf.eh;

import com.hydracontrolfreak.hcf.hcfdevice.config.Watchdog;

public interface WatchdogManager {

	void subscribe(Watchdog watchdog);

	void resetWatchdog(String eventName);
}
