package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.FreakDevice;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.FreakDevice;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.FreakJSON;
import com.hydracontrolfreak.hcf.json.FreaksJSON;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns={"/freaksgetjson"})
public class FreaksGetJSON extends HttpServlet {
	private static final long serialVersionUID = 3003922593335993839L;
	private static final Logger logger = Logger.getLogger(FreaksGetJSON.class);
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

		// Gson gson = new
		// GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		PrintWriter out = response.getWriter();

		FreaksJSON freaksJSON = new FreaksJSON(true);

		for (FreakDevice freakDevice : hcfConfig.getFreakConfig().getFreakMap()
				.values()) {

			FreakJSON freakJSON = new FreakJSON();
			freakJSON.setName(freakDevice.getName());
			freakJSON.setDescription(freakDevice.getDescription());
			freakJSON.setHostname(freakDevice.getHostname());
			freakJSON.setPort(freakDevice.getPort());
			freakJSON.setProtocol(freakDevice.getProtocol());
			freakJSON.setVerifyHostname(freakDevice.isVerifyHostname());
			freakJSON.setUsername(freakDevice.getUsername());
			freakJSON.setPassword(freakDevice.getPassword());
			freakJSON.setGuest(freakDevice.isGuest());

			freaksJSON.getFreaks().add(freakJSON);
		}

		out.print(gson.toJson(freaksJSON));
	}

}
