package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.SynthTrigger;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.SynthTriggerImpl;
import com.hydracontrolfreak.hcf.eventlib.ConfigureCombinationEventsEvent;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.ResultMessage;
import com.hydracontrolfreak.hcf.json.SyntheticJSON;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns={"/synthetics"})
public class Synthetics extends HttpServlet {
	private static final long serialVersionUID = -4211061015117394122L;
	private static final Logger logger = Logger.getLogger(Synthetics.class);
	HcfDeviceConfig hcfConfig;
	FreakApi freak;

	@Override
	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		response.setContentType("text/html");

		hcfConfig = freak.getHcfConfig();
		if (logger.isDebugEnabled())
			logger.debug("hcfConfig: " + hcfConfig);

		if (request.getMethod().equals("POST")) {
			if (logger.isDebugEnabled())
				logger.debug("Posting buttongroups's");
			response.setContentType("application/json");

			if (logger.isDebugEnabled())
				logger.debug("content-type: "
						+ request.getHeader("content-type"));

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

			// SyntheticJSON
			if (logger.isDebugEnabled())
				logger.debug("About to convert");
			SyntheticJSON[] syntheticList = fromGson.fromJson(body,
					SyntheticJSON[].class);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			if (logger.isDebugEnabled())
				logger.debug("syntheticList has become: "
						+ gson.toJson(syntheticList));

			Map<String, SynthTrigger> triggerMap = new HashMap<String, SynthTrigger>();
			for (SyntheticJSON syntheticJSON : syntheticList) {
				SynthTrigger synthTrigger = new SynthTriggerImpl(
						syntheticJSON.getResult(),
						syntheticJSON.getWithinSeconds(),
						syntheticJSON.getTriggerEventNames());
				triggerMap.put(syntheticJSON.getResult(), synthTrigger);
			}
			hcfConfig.getSynthTriggerConfig().setTriggerMap(triggerMap);

			// Now save to disk
			freak.saveConfig();

			freak.sendEvent(new ConfigureCombinationEventsEvent());

			ResultMessage resultMessage = new ResultMessage(true, "");

			PrintWriter out = response.getWriter();

			out.print(gson.toJson(resultMessage));
		} else {
			RequestDispatcher view = request
					.getRequestDispatcher("jsp/Synthetics.jsp");
			view.forward(request, response);
		}
	}

}