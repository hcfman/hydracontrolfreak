package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.Schedule;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.config.Schedule;
import com.hydracontrolfreak.hcf.json.ScheduleJSON;
import com.hydracontrolfreak.hcf.json.SchedulesJSON;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns={"/schedulesgetjson"})
public class SchedulesGetJSON extends HttpServlet {
	private static final long serialVersionUID = -1411170537048259924L;
	private static final Logger logger = Logger
			.getLogger(SchedulesGetJSON.class);
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

		SchedulesJSON schedulesJSON = new SchedulesJSON(true);

		for (Schedule schedule : hcfConfig.getScheduleConfig().getSchedules()) {
			ScheduleJSON scheduleJSON = new ScheduleJSON();
			scheduleJSON.setEventName(schedule.getEventName());
			scheduleJSON.setName(schedule.getName());
			scheduleJSON.setTimeSpec(schedule.getTimeSpec());

			schedulesJSON.getSchedules().add(scheduleJSON);
		}

		out.print(gson.toJson(schedulesJSON));
	}

}
