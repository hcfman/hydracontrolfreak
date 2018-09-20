package com.hydracontrolfreak.hcf.streamer;

import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns={"/buttons"})
public class Buttons extends HttpServlet {
	private static final long serialVersionUID = -8204105766138157239L;
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

		String groupName = request.getParameter("group");
		
		if (groupName == null || groupName.trim().equals(""))
			throw new ServletException("You must pass a group name");
		
		request.setAttribute("groupName", groupName);

		RequestDispatcher view = request.getRequestDispatcher("jsp/buttons.jsp");
		view.forward(request, response);
		
	}

}
