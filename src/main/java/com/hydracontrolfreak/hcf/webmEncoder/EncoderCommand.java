package com.hydracontrolfreak.hcf.webmEncoder;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EncoderCommand implements Comparable<EncoderCommand>, Runnable {
	private static final Logger logger = Logger.getLogger(EncoderCommand.class);
	private EncoderRequest request;
	private String sourceDir;
	private int priority;
	private long eventTime;
	private int rate;
	private String title;
	private String targetWebmFile;
	private String targetHtmlFile;
	
	
	public EncoderCommand(EncoderRequest request, String sourceDir,
                          int priority, long eventTime,
                          int rate, String title, String targetWebmFile, String targetHtmlFile ) {
		this.request = request;
		this.sourceDir = sourceDir;
		this.priority = priority;
		this.eventTime = eventTime;
		this.rate = rate;
		this.title = title;
		this.targetWebmFile = targetWebmFile;
		this.targetHtmlFile = targetHtmlFile;
	}
	
	public EncoderCommand(EncoderRequest request) {
		this.request = request;
	}

	public EncoderRequest getRequest() {
		return request;
	}
	
	public void setRequest(EncoderRequest request) {
		this.request = request;
	}
	
	public String getSourceDir() {
		return sourceDir;
	}

	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

	public int getRate() {
		return rate;
	}
	
	public void setRate(int rate) {
		this.rate = rate;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTargetHtmlFile() {
		return targetHtmlFile;
	}

	public void setTargetHtmlFile(String targetHtmlFile) {
		this.targetHtmlFile = targetHtmlFile;
	}

	public String getTargetWebmFile() {
		return targetWebmFile;
	}

	public void setTargetWebmFile(String targetWebmFile) {
		this.targetWebmFile = targetWebmFile;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public long getEventTime() {
		return eventTime;
	}

	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}

	public int compareTo( EncoderCommand o ) {
		if ( o.getRequest() == EncoderRequest.QUIT)
			return 1;
		if ( o.getEventTime() > this.getEventTime() )
			return 1;
		if ( o.getEventTime() < this.getEventTime())
			return -1;
		if ( o.getPriority() < this.getPriority())
			return 1;
		if ( o.getPriority() > this.getPriority())
			return -1;
		return 0;
	}

	
	/*
	 * Runnable support methods below
	 */
	
	private String escapeHtml(String s) {
		String newString = s.replaceAll("\\&", "&amp;");
		newString = newString.replaceAll("<", "&lt;");
		newString = newString.replaceAll(">", "&gt;");
		return newString;
	}
	
	private String basename(String s) {
		Pattern p = Pattern.compile("^.*?([^/]*)$");

		Matcher m = p.matcher(s);
		if (m.matches()) {
			return s.substring(m.start(1), m.end(1));
		}
		return s;
	}

	private void createHtml(String htmlFilename, String webmFilename,
                            String title) throws IOException {
		String finalHtml = EncoderImpl.getHtmltemplate().replaceAll("<kc-title>",
				escapeHtml(title));
		finalHtml = finalHtml.replaceAll("<kc-src>", basename(webmFilename));

		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(
				new File(htmlFilename)));

		osw.write(finalHtml);
		osw.close();
	}
	
	private void encodeFile(String sourceDir, int rate, String title,
                            String targetWebmFile, String targetHtmlFile) {
		if (logger.isDebugEnabled()) logger.debug("targetWebmFile: " + targetWebmFile);
		if (logger.isDebugEnabled()) logger.debug("targetHtmlFile: " + targetHtmlFile);
		List<String> command = new ArrayList<String>();
		command.add("ffmpeg");
		command.add("-y");
		command.add("-r");
		command.add(Integer.toString(rate));

		command.add("-i");
		command.add(EncoderImpl.getSourceprefix() + sourceDir + "/%04d.jpg");
		command.add(targetWebmFile);

		StringBuilder sb = new StringBuilder();
		for (String c : command)
			sb.append(c + " ");

		logger.debug("WebM Encode: " + sb.toString());

		ProcessBuilder pb = new ProcessBuilder(command);

		pb.redirectErrorStream(true);

		try {
			Process process = pb.start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				if (logger.isDebugEnabled()) logger.debug(line);
			}
			br.close();

			try {
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			process.getErrorStream().close();
			process.getInputStream().close();
			process.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			createHtml(targetHtmlFile, targetWebmFile, title);
		} catch (IOException e) {
			logger.error("Can't create html file " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void run() {
		encodeFile(getSourceDir(),
				getRate(),
				getTitle(),
				getTargetWebmFile(),
				getTargetHtmlFile());
	}
}
