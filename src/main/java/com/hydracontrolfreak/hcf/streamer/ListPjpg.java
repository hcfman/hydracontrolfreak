package com.hydracontrolfreak.hcf.streamer;

import com.hydracontrolfreak.hcf.hcfdevice.configImpl.VideoType;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns={"/listpjpg"})
public class ListPjpg extends HttpServlet {
	private static final long serialVersionUID = -7817873548328736880L;
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;
	private ListService listService;

	@Override
	public void init() throws ServletException {
//		freak = Freak.getInstance();
//		listService = new ListService();
	}

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		if (listService == null)
			listService = new ListService();

		listService.service(freak, VideoType.PJPEG, request, response);
	}

}
