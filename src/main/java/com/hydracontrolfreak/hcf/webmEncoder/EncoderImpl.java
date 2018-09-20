package com.hydracontrolfreak.hcf.webmEncoder;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.*;

public class EncoderImpl implements Encoder {
	private static Logger logger = org.apache.log4j.Logger
			.getLogger(EncoderImpl.class);
	private static String sourcePrefix;
	private Thread thread;
	private static String htmlTemplate;
	private ExecutorService executorService;
	LinkedBlockingQueue<Integer> tempIndexQueue;

	PriorityBlockingQueue<EncoderCommand> encoderQueue;

	public EncoderImpl(String base) throws IOException {
		this(base, 1);
	}

	public EncoderImpl(String sourcePrefix, int numThreads) throws IOException {
		EncoderImpl.sourcePrefix = sourcePrefix;
		setEncoderQueue(new PriorityBlockingQueue<EncoderCommand>());
		EncoderImpl.htmlTemplate = getTemplate("htmlTemplate.htm");

//		executorService = Executors.newFixedThreadPool(numThreads,
//				new EncoderThreadFactory());
		
		executorService = new ThreadPoolExecutor(numThreads, numThreads,
                0L, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(),
                new EncoderThreadFactory());
	    }
		
	String getTemplate(String filename) {
		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(filename);
		BufferedReader inStream;

		inStream = new BufferedReader(new InputStreamReader(inputStream));

		char buffer[] = new char[1000];
		StringBuilder sb = new StringBuilder();
		try {
			int len;
			while ((len = inStream.read(buffer, 0, buffer.length)) > 0) {
				sb.append(buffer, 0, len);
				if (logger.isDebugEnabled()) logger.debug("Template read " + len);
			}

			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	public void encode(String sourceDir, int priority, long eventTime,
                       int rate, String title, String targetWebmFile, String targetHtmlFile) {

		encoderQueue.add(new EncoderCommand(EncoderRequest.ENCODE, sourceDir,
				priority, eventTime, rate, title, targetWebmFile,
				targetHtmlFile));
	}

	Thread eventListener() {
		Thread thread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					EncoderCommand command = null;
					try {
						command = encoderQueue.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
						continue;
					}

					final EncoderCommand ec = command;

					switch (ec.getRequest()) {
					case QUIT:
						executorService.shutdown();

						return;
					case ENCODE:
						try {
							executorService.execute(ec);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}

			}
		});

		thread.setName("encoder-service");

		thread.setDaemon(true);
		return thread;
	}

	public void start() {
		logger.info("Starting encoder service");
		thread = eventListener();
		thread.start();
	}

	public void stop() {
		logger.info("Stopping encoder service");
		encoderQueue.add(new EncoderCommand(EncoderRequest.QUIT));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public PriorityBlockingQueue<EncoderCommand> getEncoderQueue() {
		return encoderQueue;
	}

	public void setEncoderQueue(
			PriorityBlockingQueue<EncoderCommand> encoderQueue) {
		this.encoderQueue = encoderQueue;
	}

	public static String getHtmltemplate() {
		return htmlTemplate;
	}

	public static String getSourceprefix() {
		return sourcePrefix;
	}
	
}
