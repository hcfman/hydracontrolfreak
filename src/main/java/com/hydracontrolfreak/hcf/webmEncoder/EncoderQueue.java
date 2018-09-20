package com.hydracontrolfreak.hcf.webmEncoder;

import java.util.concurrent.PriorityBlockingQueue;

public class EncoderQueue {
	
	private PriorityBlockingQueue<EncoderRequest> queue = new PriorityBlockingQueue<EncoderRequest>();

	public PriorityBlockingQueue<EncoderRequest> getQueue() {
		return queue;
	}

	public void setQueue(PriorityBlockingQueue<EncoderRequest> queue) {
		this.queue = queue;
	}

}
