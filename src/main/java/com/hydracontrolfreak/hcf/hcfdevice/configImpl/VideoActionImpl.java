package com.hydracontrolfreak.hcf.hcfdevice.configImpl;

import com.hydracontrolfreak.hcf.hcfdevice.config.AbstractAction;
import com.hydracontrolfreak.hcf.hcfdevice.config.ActionType;
import com.hydracontrolfreak.hcf.hcfdevice.config.HasCameras;

import java.util.SortedSet;
import java.util.TreeSet;

public class VideoActionImpl extends AbstractAction implements HasCameras {
	private SortedSet<Integer> cameraSet = new TreeSet<Integer>();

	public VideoActionImpl(String name, String eventName, String description, SortedSet<Integer> cameraSet) {
		setName(name);
		setEventName(eventName);
		setDescription(description);
		setActionType(ActionType.ACTION_VIDEO);
		this.cameraSet = cameraSet;
	}

	public SortedSet<Integer> getCameraSet() {
		return cameraSet;
	}

}
