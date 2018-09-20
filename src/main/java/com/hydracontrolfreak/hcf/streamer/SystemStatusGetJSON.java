package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.diskwatchdog.SpaceJSON;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.hcfdevice.config.CameraDevice;
import com.hydracontrolfreak.hcf.hcfdevice.config.DiskState;
import com.hydracontrolfreak.hcf.hcfdevice.config.PhidgetDevice;
import com.hydracontrolfreak.hcf.diskwatchdog.SpaceJSON;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunner;
import com.hydracontrolfreak.hcf.freakutils.ScriptRunnerResult;
import com.hydracontrolfreak.hcf.hcfdevice.config.CameraDevice;
import com.hydracontrolfreak.hcf.hcfdevice.config.DiskState;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.hcfdevice.config.PhidgetDevice;
import com.hydracontrolfreak.hcf.json.CameraStatusJSON;
import com.hydracontrolfreak.hcf.json.SystemStatusJSON;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns={"/systemstatusgetjson"})
public class SystemStatusGetJSON extends HttpServlet {
	private static final Logger logger = Logger
			.getLogger(SystemStatusGetJSON.class);
	private static final long serialVersionUID = -4661056813441955903L;
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;

	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	private SpaceJSON getSpace() {
		ScriptRunner scriptRunner = new ScriptRunner();
		;
		ScriptRunnerResult scriptRunnerResult = scriptRunner.spawn(
				freak.getHcfBase() + "/bin/suwrapper", freak.getHcfBase()
						+ "/bin/getspace.sh", "getspace.sh");
		SpaceJSON spaceJSON = new SpaceJSON();
		if (scriptRunnerResult.getResult() == 0) {
			Gson fromGson = new Gson();
			spaceJSON = fromGson.fromJson(scriptRunnerResult.getOutput(),
					SpaceJSON.class);

			return spaceJSON;
		}

		return null;
	}

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		response.setContentType("application/json");

		hcfConfig = freak.getHcfConfig();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		PrintWriter out = response.getWriter();

		SystemStatusJSON systemStatusJSON = new SystemStatusJSON(true);

		for (CameraDevice cameraDevice : hcfConfig.getCameraConfig()
				.getCameraDevices().values()) {
			if (logger.isDebugEnabled())
				logger.debug("Looping for cam: " + cameraDevice.getIndex());
			CameraStatusJSON cameraStatusJSON = new CameraStatusJSON(
					cameraDevice.getName(), cameraDevice.getIndex(),
					cameraDevice.isUp());

			systemStatusJSON.getCameraStatus().add(cameraStatusJSON);
		}
		Freak freak = Freak.getInstance();

		systemStatusJSON.setRfxcomStatus(freak.getRfxcomHandler()
				.getRfxcomController().isConnected());

		for (PhidgetDevice phidgetDevice : hcfConfig.getPhidgetConfig()
				.getPhidgetMap().values()) {
			systemStatusJSON.getPhidgetStatus().put(
					phidgetDevice.getSerialNumber(),
					phidgetDevice.isConnected());
		}

		if (hcfConfig.getDiskConfig().getDiskState() != DiskState.ALL_GOOD) {
			systemStatusJSON.setDiskUp(false);
			systemStatusJSON.setDiskMessage(hcfConfig.getDiskConfig()
					.getLastMessage());
		} else {
			systemStatusJSON.setDiskUp(true);
			SpaceJSON spaceJSON = getSpace();
			if (spaceJSON != null) {
				long used = spaceJSON.getUsed();
				long total = spaceJSON.getAvailable() + used;
				systemStatusJSON
						.setDiskMessage(spaceJSON != null ? ("Size: "
								+ (total / 1024) + "MB, Used: " + (spaceJSON
								.getUsed() / 1024))
								+ "MB ("
								+ (used * 100 / total) + "%)" : "");
			} else {
				systemStatusJSON
						.setDiskMessage("Failed to determine space used");
			}
		}

		out.print(gson.toJson(systemStatusJSON));
	}

}
