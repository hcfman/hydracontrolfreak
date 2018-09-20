package com.hydracontrolfreak.hcf.streamer;

import com.hydracontrolfreak.hcf.hcfdevice.config.FreakDevice;
import com.hydracontrolfreak.hcf.hcfdevice.config.FreakDevice;
import com.hydracontrolfreak.hcf.json.CameraListJSON;

import java.util.concurrent.LinkedBlockingQueue;

public class RemoteFreakSpec {
	private FreakDevice freakDevice;
	private LinkedBlockingQueue<CameraListJSON> queue = new LinkedBlockingQueue<CameraListJSON>();

	public RemoteFreakSpec() {
	}

	public RemoteFreakSpec(FreakDevice freakDevice) {
		this.freakDevice = freakDevice;
	}

	public FreakDevice getFreakDevice() {
		return freakDevice;
	}

	public void setFreakDevice(FreakDevice freakDevice) {
		this.freakDevice = freakDevice;
	}

	public LinkedBlockingQueue<CameraListJSON> getQueue() {
		return queue;
	}

	public void setQueue(LinkedBlockingQueue<CameraListJSON> queue) {
		this.queue = queue;
	}

	@Override
	public String toString() {
		return "RemoteFreakSpec [freakDevice=" + freakDevice + ", queue="
				+ queue + "]";
	}
	
}
