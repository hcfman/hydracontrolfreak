package com.hydracontrolfreak.hcf.dvr;

public class Request {
	static final int SAVE = 1;
	static final int QUIT = 2;
	
	int command;
	long eventTime;
	String description;

	Request( int command, long time, String description ) {
		this.command = command;
		
		eventTime = time;
		this.description = description;
	}

}
