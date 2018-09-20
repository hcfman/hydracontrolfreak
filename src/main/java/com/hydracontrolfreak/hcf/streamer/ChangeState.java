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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns={"/changestate"})
public class ChangeState extends HttpServlet {
	private static final Logger logger = Logger.getLogger(ChangeState.class);
	private static final long serialVersionUID = -6553005828381942832L;
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

		hcfConfig = freak.getHcfConfig();

		if (request.getMethod().equals("POST")) {
			List<String> tagList = new ArrayList<String>();

			Pattern pattern = Pattern.compile("^state(\\S+)$");
			for (@SuppressWarnings("rawtypes")
                 Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
				String tagName = (String) e.nextElement();
				Matcher matcher = pattern.matcher(tagName);

				if (matcher.matches()) {
					String indexString = matcher.group(1);
					String parameter = request.getParameter("values"
							+ indexString);
					if (parameter != null && !parameter.equals("")) {
						if (logger.isDebugEnabled())
							logger.debug("Clearing " + parameter);
						tagList.add(parameter);
					}
				}
			}

			freak.clearTags(tagList);

			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();

			String eventName = request.getParameter("event");
			out.println("Ok");
			out.close();
		} else {
			request.setAttribute("tagList", freak.getTagList());

			RequestDispatcher view = request
					.getRequestDispatcher("jsp/ChangeState.jsp");
			view.forward(request, response);
		}

	}
}
