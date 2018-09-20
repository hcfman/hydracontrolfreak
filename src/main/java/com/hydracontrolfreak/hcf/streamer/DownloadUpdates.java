package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.Progress;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.ResultMessage;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@WebServlet(urlPatterns={"/downloadupdates"})
public class DownloadUpdates extends HttpServlet {
	private static final long serialVersionUID = -4622657161443031570L;
	private static final Logger logger = Logger
			.getLogger(DownloadUpdates.class);
	private static final Logger opLogger = Logger.getLogger("operations");
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;

	@Override
	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	private boolean startDownload(UpdateCheckJSON updateCheckJSON) {
		if (freak.getUpdating().get()) {
			opLogger.error("The system is already updating");
			return false;
		}

		synchronized (freak) {
			Executor executor = Executors.newSingleThreadExecutor();
			Thread downloadThread = new Thread(new Downloader(freak,
					updateCheckJSON));
			downloadThread.setName("downloader");
			hcfConfig.getDownloadConfig().setThread(downloadThread);
			hcfConfig.getDownloadConfig().setProgressQueue(
					new LinkedBlockingQueue<Progress>());
			executor.execute(downloadThread);
		}

		return true;
	}

	@RequestMapping("/downloadupdates")
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		response.setContentType("text/html");

		hcfConfig = freak.getHcfConfig();

		ResultMessage resultMessage;

		GetUpdateCheckJSON checkUpdates = new GetUpdateCheckJSON(freak,
				hcfConfig);
		PrintWriter out = response.getWriter();

		UpdateCheckJSON updateCheckJSON = null;
		try {
			updateCheckJSON = checkUpdates.getUpdateCheckResult();
		} catch (Exception e) {
			opLogger.error("Failed to check for updates");
			resultMessage = new ResultMessage(false,
					"Failed to check for updates");

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			out.print(gson.toJson(resultMessage));
			return;
		}

		if (!startDownload(updateCheckJSON))
			resultMessage = new ResultMessage(false,
					"Failed to update, check the system logs for details");
		else
			resultMessage = new ResultMessage(true, "");

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.print(gson.toJson(resultMessage));
	}
}
