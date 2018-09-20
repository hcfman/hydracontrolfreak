package com.hydracontrolfreak.hcf.webmEncoder;

import java.util.concurrent.ThreadFactory;

public class EncoderThreadFactory implements ThreadFactory {
	private static int count = 0;
	
	public Thread newThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setName("encoder-" + ++count);
		return thread;
	}

}
