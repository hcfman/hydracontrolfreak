package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.Profile;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.ProfileJSON;
import com.hydracontrolfreak.hcf.json.ProfilesJSON;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns={"/profilesgetjson"})
public class ProfilesGetJSON extends HttpServlet {
	private static final long serialVersionUID = 2358556701528161520L;
	private static final Logger logger = Logger
			.getLogger(ProfilesGetJSON.class);
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

		ProfilesJSON profilesJSON = new ProfilesJSON(true);

		for (Profile profile : hcfConfig.getProfileConfig().getProfileList()) {

			ProfileJSON profileJSON = new ProfileJSON();
			profileJSON.setName(profile.getName());
			profileJSON.setDescription(profile.getDescription());
			profileJSON.setTagname(profile.getTagName());
			profileJSON.setTimeSpecs(profile.getValidTimes());

			profilesJSON.getProfilesJSON().add(profileJSON);
		}

		out.print(gson.toJson(profilesJSON));
	}

}
