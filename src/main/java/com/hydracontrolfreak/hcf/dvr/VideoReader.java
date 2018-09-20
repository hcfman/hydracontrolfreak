package com.hydracontrolfreak.hcf.dvr;

import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.eventlib.HttpAuthenticator;
import com.hydracontrolfreak.hcf.eventlib.VideoTriggerEvent;
import com.hydracontrolfreak.hcf.hcfdevice.config.CameraDevice;
import com.hydracontrolfreak.hcf.hcfdevice.config.DiskState;
import com.hydracontrolfreak.hcf.webmEncoder.Encoder;
import com.hydracontrolfreak.hcf.eventlib.EventType;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoReader {
	private static final Logger logger = Logger.getLogger(VideoReader.class);
	private static final Logger oplogger = Logger.getLogger("operations");
	private HcfDeviceConfig hcfConfig;

	private FreakApi freak;
	private AtomicBoolean shuttingDown = new AtomicBoolean();
	private int connectTimeout;

	static final int SAVE = 1;
	static final int QUIT = 2;

	private CameraDevice cameraDevice;
	private Encoder encoder;
	private int camIndex;
	private String cam;
	private String urlString;
	private CameraDevice camera;
	private int bufferSeconds;
	private int framesPerSecond;
	private int continueSeconds;
	private boolean cachingAllowed;

	private boolean isConnected = false;

	private LinkedBlockingQueue<Event> queue;
	private List<EventHeader> activeEventList;

	// Handle frame rates
	private FrameRateCorrector frameRateCorrector = new FrameRateCorrector();

	public VideoReader(CameraDevice cameraDevice, int connectTimeout,
			FreakApi freak, Encoder encoder, CameraDevice camera,
			LinkedBlockingQueue<Event> queue) {
		this.freak = freak;
		this.cameraDevice = cameraDevice;
		this.connectTimeout = connectTimeout;

		this.encoder = encoder;
		this.camera = camera;
		camIndex = camera.getIndex();
		cam = Integer.toString(camera.getIndex());
		urlString = camera.getUrl();
		this.queue = queue;
		activeEventList = new LinkedList<EventHeader>();

		bufferSeconds = camera.getBufferSeconds();
		framesPerSecond = camera.getFramesPerSecond();
		continueSeconds = camera.getContinueSeconds();
		cachingAllowed = camera.isCachingAllowed();

		if (logger.isDebugEnabled())
			logger.debug("Cam " + camera.getIndex() + " bufferSeconds = "
					+ bufferSeconds);
		if (logger.isDebugEnabled())
			logger.debug("Cam " + camera.getIndex()
					+ " bufferFramesPerSecond = " + framesPerSecond);
		if (logger.isDebugEnabled())
			logger.debug("Cam " + camera.getIndex() + " continueSeconds = "
					+ continueSeconds);
	}

	private String boundary;

	void getBoundary(HttpURLConnection conn) throws IOException {
		if (conn == null) {
			if (logger.isDebugEnabled())
				logger.debug("Trying to get a boundary header from a null connection");
			throw new IOException(
					"Can't find boundary specification in headers, null connection");
		}

		if (logger.isDebugEnabled())
			logger.debug("Check content type for connection: " + conn);
		String contentType = conn.getHeaderField("Content-Type");

		if (contentType == null) {
			if (logger.isDebugEnabled())
				logger.debug("Response code: " + conn.getResponseCode());
			if (logger.isDebugEnabled())
				logger.debug("ContentType is null");
			throw new IOException(
					"Can't find boundary specification in headers, ContentType is null");
		}
		int startPos = contentType.indexOf("boundary=");

		if (logger.isDebugEnabled())
			logger.debug("ContentType value is " + contentType);
		if (startPos < 0)
			throw new IOException(
					"Can't find boundary specification in headers, string doesn't match for url "
							+ conn.getURL() + " Found ContentType value of: "
							+ contentType);

		boundary = contentType.substring(startPos + 9).trim();
		if (logger.isDebugEnabled())
			logger.debug("Found boundary, it is " + boundary);
	}

	public void start() {
		hcfConfig = freak.getHcfConfig();

		Thread videoThread = new Thread(new Runnable() {

			public void run() {
				if (logger.isDebugEnabled())
					logger.debug("STARTING VIDEO THREAD: " + camera.getIndex());
				while (true) {
					try {
						if (logger.isDebugEnabled())
							logger.debug("Try and read video for: "
									+ camera.getIndex());
						readVideo();
						if (logger.isDebugEnabled())
							logger.debug("Have read video for: "
									+ camera.getIndex());
					} catch (IOException e) {
						if (isConnected)
							oplogger.info("Camera " + camIndex
									+ " disconnected");
						isConnected = false;
						cameraDevice.setUp(false);
						if (logger.isDebugEnabled())
							logger.debug("Exception caught reading video: "
									+ e.getMessage());
						expireEvents(Long.MAX_VALUE);

						// If shutting down, return from the thread
						if (shuttingDown.get()) {
							if (logger.isDebugEnabled())
								logger.debug("VIDEO READER SHUTTING DOWN, RETURNING FROM THREAD (Exception)");
							return;
						}

						try {
							Thread.sleep(30000);
						} catch (InterruptedException e1) {
							if (logger.isDebugEnabled())
								logger.debug("Interrupted Exception", e1);
						}
						if (logger.isDebugEnabled())
							logger.debug("Try and re-establishing connection for cam: "
									+ cam);
					} catch (URISyntaxException e) {
						if (logger.isDebugEnabled())
							logger.debug("URI syntax error reading video: "
									+ e.getMessage());
					}

					// If shutting down, return from the thread
					if (shuttingDown.get()) {
						if (logger.isDebugEnabled())
							logger.debug("VIDEO READER SHUTTING DOWN, RETURNING FROM THREAD (Exception)");
						return;
					}
				}
			}

		});

		videoThread.setName("video-reader-" + camera.getIndex());
		videoThread.start();
	}

	void expireEvents(long time) {
		Iterator<EventHeader> i = activeEventList.iterator();
		while (i.hasNext()) {
			EventHeader e = i.next();
			if (time > e.expireTime) {
				e.printWriter.println("end");
				e.getPrintWriter().close();
				logger.debug("Encoding camera " + cam);

				// Create webm target dir
				StringBuffer sb = new StringBuffer();
				sb.append(freak.getHcfBase());
				sb.append("/disk/hcf/webm/");
				sb.append(cam);
				sb.append("/");

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date d = new Date(e.getEventTime());
				String dateString = dateFormat.format(d);
				sb.append(dateString);
				sb.append("/");

				new File(sb.toString()).mkdirs();

				// Only encode if the disk is good
				if (hcfConfig.getDiskConfig().getDiskState() == DiskState.ALL_GOOD) {
					encoder.encode(e.getDirname(), camera.getPriority(),
							e.getEventTime(), 5, e.getDescription(),
							sb.toString() + e.eventTime + ".webm",
							sb.toString() + e.eventTime + ".htm");
				}

				i.remove();
			}
		}
	}

	String imageFilename(String dirname, int count) {
		StringBuffer sb = new StringBuffer();
		Formatter formatter = new Formatter(sb, Locale.ENGLISH);
		String s = formatter.format(sb + "%s/%04d.jpg", dirname, count).toString();
		formatter.close();

		return s;
	}

	void readVideo() throws IOException, URISyntaxException {
		if (logger.isDebugEnabled())
			logger.debug("TRYING TO READ VIDEO, username: "
					+ camera.getUsername() + " password: "
					+ camera.getPassword());
		HttpAuthenticator authenticator = HttpAuthenticator.getInstance();
		authenticator.setUsername(camera.getUsername());
		authenticator.setPassword(camera.getPassword());

		URI uri = new URI(urlString);
		URL url = uri.toURL();

		// URLConnection conn = null;
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// conn = (HttpURLConnection) url.openConnection();
		if (logger.isDebugEnabled())
			logger.debug("Connection = " + conn);
		conn.setUseCaches(cachingAllowed);
		conn.setReadTimeout(connectTimeout);
		if (logger.isDebugEnabled())
			logger.debug("Got a connection for cam: " + cam + " " + conn);

		try {
			getBoundary(conn);
		} catch (Exception e1) {
			conn.disconnect();
			throw new IOException(e1);
		}
		if (logger.isDebugEnabled())
			logger.debug("Boundary = \"" + boundary + "\"");

		if (logger.isDebugEnabled())
			logger.debug("Authority = " + url.getAuthority());

		if (logger.isDebugEnabled())
			logger.debug("Keys are");
		Map<String, List<String>> m = conn.getHeaderFields();
		for (String key : m.keySet()) {
			if (logger.isDebugEnabled())
				logger.debug("key = " + key);
			for (String value : m.get(key))
				if (logger.isDebugEnabled())
					logger.debug("Value = " + value);
			if (logger.isDebugEnabled())
				logger.debug("Value = " + m.get(key));
		}

		BufferedReader bin = null;
		BufferedInputStream inStream = new BufferedInputStream(
				conn.getInputStream());
		bin = new BufferedReader(new InputStreamReader(inStream), 1);

		if (logger.isDebugEnabled())
			logger.debug("Now get images");

		GetImage imageGetter = new GetImage();

		ImageBuffer imageBuffer = null;
		long dumpTillTime = 0;
		CircularList circList = new CircularList(freak, cam, framesPerSecond
				* bufferSeconds);

		BufferDumper bufferDumper = new BufferDumper(freak);

		LinkFile linkFile = new LinkFile();

		// Before starting, check if shutting down
		if (isShuttingDown()) {
			if (logger.isDebugEnabled())
				logger.debug("VIDEO READER SHUTTING DOWN, RETURNING FROM THREAD (Before loop)");
			return;
		}

		try {
			while ((imageBuffer = imageGetter.getImage(bin, inStream, boundary)) != null) {
				if (!frameRateCorrector.add(cam, imageBuffer.getTimestamp()))
					continue;
				long currentTime = System.currentTimeMillis();
				if (!isConnected)
					oplogger.info("Camera " + camIndex + " connected");
				isConnected = true;
				cameraDevice.setUp(true);

				// Finished recording any camera?
				expireEvents(currentTime);

				// Any new cameras to record or finishing
				if (queue != null && !queue.isEmpty()) {
					Event request = queue.remove();

					if (request.getEventType() == EventType.EVENT_SHUTDOWN) {
						if (logger.isDebugEnabled())
							logger.debug("Shutting down cam (" + cam + ")");
						inStream.close();
						if (isConnected)
							oplogger.info("Camera " + camIndex
									+ " disconnected");
						isConnected = false;
						cameraDevice.setUp(false);
						expireEvents(Long.MAX_VALUE);
						setShuttingDown(true);
						return;
					} else if (request.getEventType() == EventType.EVENT_CONFIGURE) {
						inStream.close();
						conn.disconnect();
						if (isConnected)
							oplogger.debug("Camera " + camIndex
									+ " disconnected");
						isConnected = false;
						cameraDevice.setUp(false);
						if (logger.isDebugEnabled())
							logger.debug("Restarting cam (" + cam + ")");
						setShuttingDown(true);
						if (logger.isDebugEnabled())
							logger.debug("VIDEO READER SHUTTING DOWN, RETURNING FROM THREAD (Queue reader)");
						return;
					} else if (request.getEventType() == EventType.VIDEO_TRIGGER) {
						if (isConnected) {
							if (logger.isDebugEnabled())
								logger.debug("Got a save request");
							if (logger.isDebugEnabled())
								logger.debug("Got a save request for cam "
										+ camIndex);
							VideoTriggerEvent videoTriggerEvent = (VideoTriggerEvent) request;
							// Create new event
							EventHeader event = new EventHeader(freak, cam,
									videoTriggerEvent.getEventTime(),
									videoTriggerEvent.getDescription());
							int maxBufferSize = framesPerSecond * bufferSeconds;

							if (currentTime >= dumpTillTime) {
								if (logger.isDebugEnabled())
									logger.debug("DUMPING THE CIRCULAR BUFFER");
								circList.dump(event);
								circList = new CircularList(freak, cam,
										maxBufferSize);
							} else {
								/*
								 * Add file names from any other active event.
								 * There should always be at least one active
								 * event.
								 */
								if (!activeEventList.isEmpty()) {
									if (logger.isDebugEnabled())
										logger.debug("DUMPING ALREADY IN PROGRESS for cam "
												+ camIndex);
									// Create this in reverse, then add forward
									LinkedList<String> reverseList = new LinkedList<String>();

									EventHeader anEvent = activeEventList
											.get(0);

									int count = 0;
									Iterator<String> tempIt = anEvent
											.getFilelist().descendingIterator();
									// Add in reverse
									while (count < maxBufferSize
											&& tempIt.hasNext()) {
										String fname = tempIt.next();
										reverseList.add(fname);
										count++;
									}

									// Add into real list reversed again so its
									// forward
									Iterator<String> it = reverseList
											.descendingIterator();
									while (it.hasNext()) {
										// File borrowed from another event
										String fname = it.next();

										int newCount = event
												.getLastFileWritten();
										String newFilename = imageFilename(
												event.getDirname(), newCount);

										// Link existing file to this new event
										// that overlaps, starting from 0001
										// however
										if (logger.isDebugEnabled())
											logger.debug("Linking already active ("
													+ anEvent.getEventTime()
													+ ") "
													+ fname
													+ " to "
													+ newFilename);
										linkFile.link(freak.getHcfBase()
												+ "/disk/hcf/" + fname,
												freak.getHcfBase()
														+ "/disk/hcf/"
														+ newFilename);

										event.getFilelist().add(newFilename);
										event.getPrintWriter().println(
												newFilename);
									}
								}
							}

							dumpTillTime = currentTime
									+ (continueSeconds * 1000);

							event.setExpireTime(dumpTillTime);
							if (logger.isDebugEnabled())
								logger.debug("Adding event (" + event + " ["
										+ event.toString() + "])"
										+ event.getEventTime()
										+ "to the activelist for cam "
										+ camIndex);
							activeEventList.add(event);
						} // isConnected
					}

				}

				if (currentTime < dumpTillTime) {
					bufferDumper.dumpImage(cam, imageBuffer);
					String fromFilename = freak.getHcfBase()
							+ "/disk/hcf/tmp_images/" + cam + "/"
							+ imageBuffer.getTimestamp() + ".jpg";
					for (EventHeader e : activeEventList) {
						int count = e.getLastFileWritten();
						if (logger.isDebugEnabled())
							logger.debug("DUMPING CONTINUATION (event " + e
									+ " [" + e.toString() + "]) for cam "
									+ camIndex + " (" + e.getEventTime()
									+ ") FROM COUNT: " + count);
						String fname = imageFilename(e.getDirname(), count);
						if (logger.isDebugEnabled())
							logger.debug("(" + e.getEventTime() + ")ln "
									+ freak.getHcfBase()
									+ "/disk/hcf/tmp_images/" + cam + "/"
									+ imageBuffer.getTimestamp() + ".jpg"
									+ " to " + freak.getHcfBase()
									+ "/disk/hcf/" + fname);
						linkFile.link(fromFilename, freak.getHcfBase()
								+ "/disk/hcf/" + fname);

						e.getFilelist().add(fname);
						e.getPrintWriter().println(fname);
						e.getPrintWriter().flush();
					}

					// Now it's linked to all events, remove it from tmp_images
					new File(fromFilename).delete();
				} else {
					circList.add(imageBuffer);
				}
			}
		} catch (Exception e) {
			try {
				inStream.close();
			} catch (Exception e1) {
				logger.debug("Exception closing video stream and disconnecting");
			}
			conn.disconnect();
			throw new IOException(e);
		}
	}

	public boolean isShuttingDown() {
		return shuttingDown.get();
	}

	public void setShuttingDown(boolean isShuttingDown) {
		shuttingDown.set(isShuttingDown);
	}

}
