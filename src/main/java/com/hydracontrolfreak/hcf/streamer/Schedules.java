package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.Schedule;
import com.hydracontrolfreak.hcf.eventlib.ConfigureScheduleEvent;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.ResultMessage;
import com.hydracontrolfreak.hcf.json.ScheduleJSON;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns={"/schedules"})
public class Schedules extends HttpServlet {
	private static final Logger logger = Logger.getLogger(Schedules.class);
	private static final long serialVersionUID = -5956670942335268988L;
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

		hcfConfig = freak.getHcfConfig();
		if (logger.isDebugEnabled())
			logger.debug("hcfConfig: " + hcfConfig);

		if (request.getMethod().equals("POST")) {
			if (logger.isDebugEnabled())
				logger.debug("Posting schedules");
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
			if (logger.isDebugEnabled())
				logger.debug("About to convert");

			ScheduleJSON[] schedulesJSON = fromGson.fromJson(body,
					ScheduleJSON[].class);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			if (logger.isDebugEnabled())
				logger.debug("schedulesJSON has become: "
						+ gson.toJson(schedulesJSON));
			if (logger.isDebugEnabled())
				logger.debug("schedulesJSON: " + schedulesJSON);
			List<Schedule> scheduleList = new ArrayList<Schedule>();

			for (ScheduleJSON scheduleJSON : schedulesJSON)
				scheduleList.add(scheduleJSON.toSchedule());
			hcfConfig.getScheduleConfig().setSchedules(scheduleList);

			// Now save to disk
			freak.saveConfig();

			ResultMessage resultMessage = new ResultMessage(true, "");

			PrintWriter out = response.getWriter();

			out.print(gson.toJson(resultMessage));

			if (logger.isDebugEnabled())
				logger.debug("Now restart Schedules");
			freak.sendEvent(new ConfigureScheduleEvent());
		} else {
			RequestDispatcher view = request
					.getRequestDispatcher("jsp/Schedules.jsp");
			view.forward(request, response);
		}
	}

}
