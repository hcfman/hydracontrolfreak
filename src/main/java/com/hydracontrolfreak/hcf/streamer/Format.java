package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.DiskState;
import com.hydracontrolfreak.hcf.hcfdevice.config.Progress;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.ResultMessage;
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

@WebServlet(urlPatterns={"/format"})
public class Format extends HttpServlet {
	private static final long serialVersionUID = -578974372574103580L;
	private static final Logger logger = Logger.getLogger(Format.class);
	private static final Logger opLogger = Logger.getLogger("operations");
	private HcfDeviceConfig hcfConfig;
	private FreakApi freak;

	@Override
	public void init() throws ServletException {
//		freak = Freak.getInstance();
	}

	private void startFormat() {
		Executor executor = Executors.newSingleThreadExecutor();
		Thread formatThread = new Thread(new Formatter(freak));
		formatThread.setName("formatter");
		hcfConfig.getDiskConfig().setThread(formatThread);
		hcfConfig.getDiskConfig().setProgressQueue(
				new LinkedBlockingQueue<Progress>());
		executor.execute(formatThread);
	}

	@RequestMapping("/format")
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (freak == null)
			freak = Freak.getInstance();

		response.setContentType("text/html");

		hcfConfig = freak.getHcfConfig();

		ResultMessage resultMessage = new ResultMessage(true, "");
		if (!hcfConfig.getSettingsConfig().isCheckMount()) {
			resultMessage = new ResultMessage(false,
					"This HCF has no sddisk, no formatting required");
			return;
		} else {
			if (hcfConfig.getDiskConfig().getDiskState() == DiskState.NO_DISK) {
				resultMessage = new ResultMessage(false,
						"Please insert a disk first");
			} else if (hcfConfig.getDiskConfig().getDiskState() == DiskState.FORMATTING) {
				resultMessage = new ResultMessage(
						false,
						"You are already formatting. Please wait till it's finished. If this takes an \"extremely\" long time (More than an hour for example), then turn the power off and then on again");
			} else if (freak.getUpdating().get()) {
				resultMessage = new ResultMessage(false,
						"Can't format disk, an update is in progress");
			} else {
				startFormat();
			}
		}

		PrintWriter out = response.getWriter();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.print(gson.toJson(resultMessage));
	}

}
