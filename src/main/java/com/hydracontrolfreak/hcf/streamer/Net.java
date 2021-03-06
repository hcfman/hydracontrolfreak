package com.hydracontrolfreak.hcf.streamer;

import com.hydracontrolfreak.hcf.hcfdevice.config.HttpTrigger;
import com.hydracontrolfreak.hcf.eventlib.HttpTriggerEvent;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.config.HttpTrigger;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(urlPatterns={"/net", "/guest/net"})
public class Net extends HttpServlet {
	private static final long serialVersionUID = 8142007339282202725L;
	private static final Logger logger = Logger.getLogger(Net.class);
	private static final Logger opLogger = Logger.getLogger("operations");
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;
	private boolean guest;
	private String failReason;

	@Override
	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	private boolean isAllowed(String eventName) {
		for (List<HttpTrigger> httpTriggers : hcfConfig.getHttpConfig()
				.getGroupMap().values()) {
			for (HttpTrigger httpTrigger : httpTriggers) {
				if (eventName.equals(httpTrigger.getEventName())) {
					if (guest) {
						if (!httpTrigger.isGuest()) {
							failReason = "Button doesn't allow guest access";
							return false;
						} else
							return true;
					} else {
						return true;
					}
				}
			}
		}

		failReason = "There is no button defined for this event";
		return false;
	}

	@RequestMapping(value={"/net", "/guest/net"})
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		if (!freak.isReady())
			return;

		hcfConfig = freak.getHcfConfig();

		guest = request.isUserInRole("guest");

		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();

		String eventName = request.getParameter("event");
		out.println("Ok");
		out.close();

		String clientEventTimeString = request.getParameter("eventTime");
		long clientEventTime = 0;
		try {
			if (clientEventTimeString != null)
				clientEventTime = Long.parseLong(clientEventTimeString);
		} catch (NumberFormatException e) {
			opLogger.error("Can't parse client eventTime string ("
					+ clientEventTimeString + ")");
		}

		if (eventName == null || eventName.trim().equals(""))
			return;

		if (isAllowed(eventName)) {
			if (logger.isDebugEnabled())
				logger.debug("Delivering HTTP trigger event");
			freak.sendEvent(new HttpTriggerEvent(eventName, System
					.currentTimeMillis(), clientEventTime, request
					.isUserInRole("guest") ? true : false));
		} else
			opLogger.info("Attempt to fire event \"" + eventName + "\" : "
					+ failReason);
	}

}
