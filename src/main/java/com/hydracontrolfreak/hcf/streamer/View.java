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

@WebServlet(urlPatterns={"/view"})
public class View extends HttpServlet {
	private static final Logger logger = Logger.getLogger(View.class);
	private static final long serialVersionUID = 2487130599843615117L;
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

		if (!freak.isReady())
			return;
		String cam = request.getParameter("cam");
		String event = request.getParameter("t");
		
		response.setContentType("text/html");

		request.setAttribute("cam", cam);
		request.setAttribute("t", event);
		
		RequestDispatcher view = request.getRequestDispatcher("jsp/view.jsp");
		view.forward(request, response);
	}
}
