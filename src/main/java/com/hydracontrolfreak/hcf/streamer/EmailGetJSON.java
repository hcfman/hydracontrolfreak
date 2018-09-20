package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.EmailJSON;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns={"/emailgetjson"})
public class EmailGetJSON extends HttpServlet {
	private static final long serialVersionUID = 8540530776357097308L;
	private static final Logger logger = Logger.getLogger(EmailGetJSON.class);
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

		EmailJSON emailJSON = new EmailJSON();
		emailJSON.setResult(true);
		if (hcfConfig.getEmailConfig() != null
				&& hcfConfig.getEmailConfig().getEmailProvider() != null) {
			emailJSON.setName(hcfConfig.getEmailConfig().getEmailProvider()
					.getName());
			emailJSON.setDescription(hcfConfig.getEmailConfig()
					.getEmailProvider().getDescription());
			emailJSON.setMailhost(hcfConfig.getEmailConfig().getEmailProvider()
					.getMailhost());
			emailJSON.setFrom(hcfConfig.getEmailConfig().getEmailProvider()
					.getFrom());
			emailJSON.setUsername(hcfConfig.getEmailConfig().getEmailProvider()
					.getUsername());
			emailJSON.setPassword(hcfConfig.getEmailConfig().getEmailProvider()
					.getPassword());
			emailJSON.setPort(hcfConfig.getEmailConfig().getEmailProvider()
					.getPort());
			emailJSON.setEncType(hcfConfig.getEmailConfig().getEmailProvider()
					.getEncryptionType().getEncryptionType());
		}

		out.print(gson.toJson(emailJSON));
	}

}
