package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.UpdateCheckJSON;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns={"/checkupdates"})
public class CheckUpdates extends HttpServlet {
	private static final long serialVersionUID = 5121599439000072562L;
	private static final Logger logger = Logger.getLogger(CheckUpdates.class);
	private HcfDeviceConfig hcfConfig;
	FreakApi freak;

	@Override
	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	public UpdateCheckJSON getUpdateCheckResult() throws Exception {
		String version = hcfConfig.getSettingsConfig().getVersion();
		ScriptRunner scriptRunner = new ScriptRunner();

		ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
				freak.getHcfBase() + "/bin/suwrapper", System.getenv("HOME")
						+ "/update/bin/updateavailable.sh",
				"updateavailable.sh", hcfConfig.getSettingsConfig()
						.isForceUpdate() ? "1.0" : hcfConfig
						.getSettingsConfig().getVersion());

		if (scriptRunnerResult.getResult() != 0)
			throw new Exception("Can't retrieve network settings");

		Gson fromGson = new Gson();
		UpdateCheckJSON updateCheckJSON = fromGson.fromJson(
				scriptRunnerResult.getOutput(), UpdateCheckJSON.class);

		return updateCheckJSON;
	}

	@RequestMapping("/checkupdates")
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		response.setContentType("text/html");

		hcfConfig = freak.getHcfConfig();

		UpdateCheckJSON updateCheckJSON = null;
		try {
			updateCheckJSON = getUpdateCheckResult();
		} catch (Exception e) {
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		PrintWriter out = response.getWriter();

		out.print(gson.toJson(updateCheckJSON));
	}

}
