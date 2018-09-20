package com.hydracontrolfreak.hcf.hcfdevice.configImpl;

import com.hydracontrolfreak.hcf.hcfdevice.config.AbstractAction;
import com.hydracontrolfreak.hcf.hcfdevice.config.ActionType;

public class RfxcomActionImpl extends AbstractAction {
	private String rfxcomName;

	public RfxcomActionImpl(String name, String eventName, String description, String rfxcomName) {
		setName(name);
		setEventName(eventName);
		setDescription(description);
		setActionType(ActionType.ACTION_RFXCOM);
		this.rfxcomName = rfxcomName;
	}

	public String getRfxcomName() {
		return rfxcomName;
	}

	public void setRfxcomName(String rfxcomName) {
		this.rfxcomName = rfxcomName;
	}

}
