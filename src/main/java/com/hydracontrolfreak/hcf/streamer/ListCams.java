package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.hydracontrolfreak.hcf.hcfdevice.config.CameraDevice;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.CameraDevice;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.CameraListJSON;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns={"/guest/listcams","/listcams"})
public class ListCams extends HttpServlet {
	private static final long serialVersionUID = -1702040930333520862L;
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;

	@Override
	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	@RequestMapping(value={"/guest/listcams","/listcams"})
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		if (!freak.isReady())
			return;

		hcfConfig = freak.getHcfConfig();

		hcfConfig = freak.getHcfConfig();
		CameraListJSON cameraListJSON = new CameraListJSON(true);

		for (CameraDevice cameraDevice : hcfConfig.getCameraConfig()
				.getCameraDevices().values()) {
			if (!request.isUserInRole("guest") || cameraDevice.isGuest())
				cameraListJSON.getCameraList().add(cameraDevice.getIndex());
		}

		response.setContentType("application/json");

		PrintWriter out = response.getWriter();

		Gson gson = new Gson();
		out.print(gson.toJson(cameraListJSON));
	}
}
