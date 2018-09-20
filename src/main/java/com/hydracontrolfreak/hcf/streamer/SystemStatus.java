package com.hydracontrolfreak.hcf.streamer;

import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns={"/systemstatus"})
public class SystemStatus extends HttpServlet {
	private static final long serialVersionUID = 5313689795824721936L;
	private static final Logger logger = Logger.getLogger(SystemStatus.class);
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;

	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		response.setContentType("text/html");

		hcfConfig = freak.getHcfConfig();

		RequestDispatcher view = request
				.getRequestDispatcher("jsp/SystemStatus.jsp");
		view.forward(request, response);
	}

}