package com.hydracontrolfreak.hcf.eh;

import com.hydracontrolfreak.hcf.eventlib.SyntheticTriggerEvent;

public interface EventListener {
	public void onEvent(SyntheticTriggerEvent event);
}
