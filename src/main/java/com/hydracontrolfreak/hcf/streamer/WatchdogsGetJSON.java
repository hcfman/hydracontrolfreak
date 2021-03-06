package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.Watchdog;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.WatchdogJSON;
import com.hydracontrolfreak.hcf.json.WatchdogsJSON;
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

@WebServlet(urlPatterns={"/watchdogsgetjson"})
public class WatchdogsGetJSON extends HttpServlet {
	private static final long serialVersionUID = 3157799115732191678L;
	private static final Logger logger = Logger
			.getLogger(WatchdogsGetJSON.class);
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

		WatchdogsJSON watchdogsJSON = new WatchdogsJSON(true);
		Map<String, WatchdogJSON> watchdogMap = new HashMap<String, WatchdogJSON>();
		for (String eventName : hcfConfig.getWatchdogConfig()
				.getTriggerMap().keySet()) {
			Watchdog watchdog = hcfConfig.getWatchdogConfig()
					.getTriggerMap().get(eventName);

			WatchdogJSON watchdogJSON = new WatchdogJSON();
			watchdogJSON.setTriggerEventNames(watchdog
					.getTriggerEventNames());
			watchdogJSON.setResult(watchdog.getResult());
			watchdogJSON.setWithinSeconds(watchdog.getWithinSeconds());

			watchdogMap.put(eventName, watchdogJSON);
		}

		watchdogsJSON.setWatchdogMap(watchdogMap);
		SortedSet<String> eventSet = new TreeSet<String>();
		eventSet.addAll(hcfConfig.getAvailableRfxcomEventNames());
		eventSet.addAll(hcfConfig.getAvailableButtonEventNames());
		eventSet.addAll(hcfConfig.getAvailablePhidgetEventnames());
		watchdogsJSON.setAvailableEventNames(eventSet);

		out.print(gson.toJson(watchdogsJSON));
	}

}
