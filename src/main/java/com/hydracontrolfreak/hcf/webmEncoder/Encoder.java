package com.hydracontrolfreak.hcf.webmEncoder;


public interface Encoder {
	public void encode(String sourceDir, int priority, long eventTime,
                       int rate, String title, String targetWebmFile, String targetHtmlFile);

	public void start();

	public void stop();
}
