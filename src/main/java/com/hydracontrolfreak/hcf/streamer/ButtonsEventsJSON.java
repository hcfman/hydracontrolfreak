package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.HttpTrigger;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.config.HttpTrigger;
import com.hydracontrolfreak.hcf.json.ButtonEventsJSON;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(urlPatterns={"/buttoneventsjson"})
public class ButtonsEventsJSON extends HttpServlet {
	private static final long serialVersionUID = 88412925072547649L;
	private static final Logger logger = Logger
			.getLogger(ButtonsEventsJSON.class);
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

		String groupName = request.getParameter("group");
		if (logger.isDebugEnabled())
			logger.debug("GroupName = " + groupName);

		if (groupName == null || groupName.trim().equals(""))
			throw new ServletException("You must pass a group parameter");

		// Gson gson = new
		// GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		PrintWriter out = response.getWriter();

		ButtonEventsJSON buttonsJSON = new ButtonEventsJSON(true);

		List<String> eventNames = buttonsJSON.getEventNames();
		for (HttpTrigger httpTrigger : hcfConfig.getHttpConfig().getGroupMap()
				.get(groupName)) {

			eventNames.add(httpTrigger.getEventName());
		}

		out.print(gson.toJson(buttonsJSON));
	}

}
