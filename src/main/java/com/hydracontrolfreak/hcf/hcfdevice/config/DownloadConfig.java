package com.hydracontrolfreak.hcf.hcfdevice.config;

import java.util.concurrent.LinkedBlockingQueue;

public class DownloadConfig {
	private volatile String lastMessage = "";
	private volatile Thread thread;
	private volatile LinkedBlockingQueue<Progress> progressQueue;

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public LinkedBlockingQueue<Progress> getProgressQueue() {
		return progressQueue;
	}

	public void setProgressQueue(LinkedBlockingQueue<Progress> progressQueue) {
		this.progressQueue = progressQueue;
	}

	@Override
	public String toString() {
		return "DownloadConfig [lastMessage=" + lastMessage + ", thread="
				+ thread + ", progressQueue=" + progressQueue + "]";
	}
	
}
