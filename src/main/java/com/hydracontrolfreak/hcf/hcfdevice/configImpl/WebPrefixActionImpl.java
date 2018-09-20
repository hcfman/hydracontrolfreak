package com.hydracontrolfreak.hcf.hcfdevice.configImpl;

import com.hydracontrolfreak.hcf.hcfdevice.config.AbstractAction;
import com.hydracontrolfreak.hcf.hcfdevice.config.ActionType;

public class WebPrefixActionImpl extends AbstractAction {
	private String prefix;

	public WebPrefixActionImpl() {
	}

	public WebPrefixActionImpl(String name, String eventName, String description, String prefix) {
		setName(name);
		setEventName(eventName);
		setDescription(description);
		setActionType(ActionType.ACTION_WEB_PREFIX);
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String toString() {
		return "WebPrefixActionImpl [prefix=" + prefix + "]";
	}

}
