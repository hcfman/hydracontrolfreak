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
import org.apache.log4j.Logger;


@WebServlet(urlPatterns={"/actionlog"})
public class ActionLog extends HttpServlet {
	private static final long serialVersionUID = 8391229639062607622L;
	private static final Logger logger = Logger.getLogger(ActionLog.class);
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

        response.setContentType("text/html");

		hcfConfig = freak.getHcfConfig();

		RequestDispatcher view = request
				.getRequestDispatcher("jsp/ActionLog.jsp");
		view.forward(request, response);
	}

}