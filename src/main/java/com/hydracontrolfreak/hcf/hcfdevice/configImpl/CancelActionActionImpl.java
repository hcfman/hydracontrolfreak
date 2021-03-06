package com.hydracontrolfreak.hcf.hcfdevice.configImpl;

import com.hydracontrolfreak.hcf.hcfdevice.config.AbstractAction;
import com.hydracontrolfreak.hcf.hcfdevice.config.ActionType;

public class CancelActionActionImpl extends AbstractAction {
	private String actionName;

	public CancelActionActionImpl(String name, String eventName, String description, String actionName) {
		setName(name);
		setActionType(ActionType.ACTION_CANCEL_ACTION);
		setEventName(eventName);
		setDescription(description);
		this.actionName = actionName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	@Override
	public String toString() {
		return "CancelActionActionImpl [actionName=" + actionName + "]";
	}

}
