package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.SynthTrigger;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.SyntheticJSON;
import com.hydracontrolfreak.hcf.json.SyntheticsJSON;
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
import java.util.SortedSet;
import java.util.TreeSet;

@WebServlet(urlPatterns={"/syntheticsgetjson"})
public class SyntheticsGetJSON extends HttpServlet {
	private static final long serialVersionUID = 4849780100322337750L;
	private static final Logger logger = Logger
			.getLogger(SyntheticsGetJSON.class);
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

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		PrintWriter out = response.getWriter();

		SyntheticsJSON syntheticsJSON = new SyntheticsJSON(true);
		Map<String, SyntheticJSON> syntheticMap = new HashMap<String, SyntheticJSON>();
		for (String eventName : hcfConfig.getSynthTriggerConfig()
				.getTriggerMap().keySet()) {
			SynthTrigger synthTrigger = hcfConfig.getSynthTriggerConfig()
					.getTriggerMap().get(eventName);

			SyntheticJSON syntheticJSON = new SyntheticJSON();
			syntheticJSON.setTriggerEventNames(synthTrigger
					.getTriggerEventNames());
			syntheticJSON.setResult(synthTrigger.getResult());
			syntheticJSON.setWithinSeconds(synthTrigger.getWithinSeconds());

			syntheticMap.put(eventName, syntheticJSON);
		}

		syntheticsJSON.setSyntheticMap(syntheticMap);
		SortedSet<String> eventSet = new TreeSet<String>();
		eventSet.addAll(hcfConfig.getAvailableRfxcomEventNames());
		eventSet.addAll(hcfConfig.getAvailableButtonEventNames());
		eventSet.addAll(hcfConfig.getAvailablePhidgetEventnames());
		syntheticsJSON.setAvailableEventNames(eventSet);

		out.print(gson.toJson(syntheticsJSON));
	}

}
