package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.CameraDevice;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.CameraDevice;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.CameraJSON;
import com.hydracontrolfreak.hcf.json.CamerasJSON;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

@WebServlet(urlPatterns={"/camerasgetjson"})
public class CamerasGetJSON extends HttpServlet {
	private static final long serialVersionUID = 8835361381758906332L;
	private static final Logger logger = Logger.getLogger(CamerasGetJSON.class);
	private HcfDeviceConfig hcfConfig;
	FreakApi freak;

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

		// Gson gson = new
		// GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		PrintWriter out = response.getWriter();

		CamerasJSON camerasJSON = new CamerasJSON(true);

		TreeMap<Integer, CameraDevice> tm = (TreeMap<Integer, CameraDevice>) hcfConfig
				.getCameraConfig().getCameraDevices();
		for (Map.Entry<Integer, CameraDevice> entry : tm.entrySet()) {
			CameraDevice camera = entry.getValue();

			CameraJSON cameraJSON = new CameraJSON();
			cameraJSON.setName(camera.getName());
			cameraJSON.setDescription(camera.getDescription());
			cameraJSON.setIndex(camera.getIndex());
			cameraJSON.setEnabled(camera.isEnabled());
			cameraJSON.setUrl(camera.getUrl());
			cameraJSON.setUsername(camera.getUsername());
			cameraJSON.setPassword(camera.getPassword());
			cameraJSON.setContinueSeconds(camera.getContinueSeconds());
			cameraJSON.setBufferSeconds(camera.getBufferSeconds());
			cameraJSON.setFramesPerSecond(camera.getFramesPerSecond());
			cameraJSON.setPriority(camera.getPriority());
			cameraJSON.setCachingAllowed(camera.isCachingAllowed());
			cameraJSON.setGuest(camera.isGuest());

			camerasJSON.getCameras().add(cameraJSON);
		}

		out.print(gson.toJson(camerasJSON));
	}

}
