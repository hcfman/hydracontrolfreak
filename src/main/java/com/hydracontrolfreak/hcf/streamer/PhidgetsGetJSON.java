package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.PhidgetDevice;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.PhidgetJSON;
import com.hydracontrolfreak.hcf.json.PhidgetsJSON;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns={"/phidgetsgetjson"})
public class PhidgetsGetJSON extends HttpServlet {
	private static final long serialVersionUID = 1751117016948122490L;
	private static final Logger logger = Logger
			.getLogger(PhidgetsGetJSON.class);
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

		response.setContentType("application/json");

		hcfConfig = freak.getHcfConfig();

		// Gson gson = new
		// GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		PrintWriter out = response.getWriter();

		PhidgetsJSON phidgetsJSON = new PhidgetsJSON(true);
		Map<String, PhidgetJSON> phidgetMap = new HashMap<String, PhidgetJSON>();
		for (String phidgetName : hcfConfig.getPhidgetConfig().getPhidgetMap()
				.keySet()) {
			PhidgetDevice phidgetDevice = hcfConfig.getPhidgetConfig()
					.getPhidgetMap().get(phidgetName);

			PhidgetJSON phidgetJSON = new PhidgetJSON();
			phidgetJSON.setName(phidgetName);
			phidgetJSON.setSerialNumber(Integer.toString(phidgetDevice
					.getSerialNumber()));
			phidgetJSON.setPortSize(Integer.toString(phidgetDevice
					.getPortSize()));
			phidgetJSON.setName(phidgetDevice.getName());
			phidgetJSON.setDescription(phidgetDevice.getDescription());
			phidgetJSON.setInitialInputState(phidgetDevice
					.getInitialInputState());
			phidgetJSON.setInitialOutputState(phidgetDevice
					.getInitialOutputState());
			phidgetJSON.setOffTriggerEventNames(phidgetDevice
					.getOffTriggerEventNames());
			phidgetJSON.setOnTriggerEventNames(phidgetDevice
					.getOnTriggerEventNames());

			phidgetMap.put(phidgetName, phidgetJSON);
		}

		phidgetsJSON.setPhidgetMap(phidgetMap);

		out.print(gson.toJson(phidgetsJSON));
	}

}
