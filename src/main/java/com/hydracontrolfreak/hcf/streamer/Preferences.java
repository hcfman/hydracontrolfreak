package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.eventlib.ConfigureVideoEvent;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.PreferencesJSON;
import com.hydracontrolfreak.hcf.json.ResultMessage;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet(urlPatterns={"/preferences"})
public class Preferences extends HttpServlet {
	private static final long serialVersionUID = -4525671991027438195L;
	private static final Logger logger = Logger.getLogger(Preferences.class);
	private static final Logger opLogger = Logger.getLogger("operations");
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;

	@Override
	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		response.setContentType("text/html");
		if (logger.isDebugEnabled())
			logger.debug("In Preferences saving");

		hcfConfig = freak.getHcfConfig();

		response.setContentType("application/json");

		if (logger.isDebugEnabled())
			logger.debug("content-type: " + request.getHeader("content-type"));

		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(
						inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}
		String body = stringBuilder.toString();

		if (logger.isDebugEnabled())
			logger.debug("Body: " + body);

		Gson fromGson = new Gson();
		PreferencesJSON preferencesJSON = fromGson.fromJson(body,
				PreferencesJSON.class);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (logger.isDebugEnabled())
			logger.debug("PreferencesJSON has become: "
					+ gson.toJson(preferencesJSON));

		hcfConfig.getSettingsConfig().setWebPrefix(
				preferencesJSON.getWebPrefix());

		int oldConnectTimeout = hcfConfig.getSettingsConfig()
				.getConnectTimeout();
		int newConnectTimeout = preferencesJSON.getConnectTimeout();
		hcfConfig.getSettingsConfig().setConnectTimeout(
				preferencesJSON.getConnectTimeout());

		if (oldConnectTimeout != newConnectTimeout) {
			if (logger.isDebugEnabled())
				logger.debug("oldConnectTimeout: " + oldConnectTimeout
						+ ", newConnectTimeout: " + newConnectTimeout);
			freak.sendEvent(new ConfigureVideoEvent());
		}

		hcfConfig.getSettingsConfig().setFreeSpace(
				preferencesJSON.getFreeSpace());
		hcfConfig.getSettingsConfig().setCleanRate(
				preferencesJSON.getCleanRate());
		hcfConfig.getSettingsConfig()
				.setDaysMJPG(preferencesJSON.getDaysJpeg());
		hcfConfig.getSettingsConfig().setPhoneHome(
				preferencesJSON.getPhonehomeUrl());

		ResultMessage resultMessage = new ResultMessage(true, "");

		if (freak.getUpdating().get()) {
			resultMessage.setResult(false);
			resultMessage.getMessages().add(
					"Can't save preferences, an update is in progress");
			opLogger.info("Can't save preferences, an update is in progress");
		} else {
			// Now save to disk
			freak.saveConfig();
			opLogger.info("Updated preferences");
		}

		PrintWriter out = response.getWriter();
		out.print(gson.toJson(resultMessage));

	}

}
