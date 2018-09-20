package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.rfxcomlib.RfxcomCommand;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.RfxcomCommandJSON;
import com.hydracontrolfreak.hcf.json.RfxcomsJSON;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns={"/rfxcomgetjson"})
public class RfxcomGetJSON extends HttpServlet {
	private static final long serialVersionUID = 5081505999421555600L;
	private static final Logger logger = Logger.getLogger(RfxcomGetJSON.class);
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

		// Gson gson = new Gson();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		PrintWriter out = response.getWriter();
		RfxcomsJSON rfxcomsJSON = new RfxcomsJSON();
		rfxcomsJSON.setResult(true);

		for (RfxcomCommand command : hcfConfig.getRfxcomConfig()
				.getcommandsAsList()) {
			RfxcomCommandJSON rfxcomJSON = new RfxcomCommandJSON();
			rfxcomJSON.setName(command.getName());
			rfxcomJSON.setDescription(command.getDescription());
			rfxcomJSON.setRfxcomType(command.getRfxcomType().toString()
					.replaceAll("_", " "));

			rfxcomJSON.setPacketValues1(hcfConfig.getRfxcomConfig()
					.intArrayToHexStringArray(command.getPacketValues1()));
			switch (command.getRfxcomType()) {
			case GENERIC_INPUT:
				rfxcomJSON.setEventName(command.getEventName());
				rfxcomJSON.setHysteresis(command.getHysteresis());
				rfxcomJSON.setPacketValues2(hcfConfig.getRfxcomConfig()
						.intArrayToHexStringArray(command.getPacketValues2()));
				rfxcomJSON.setPacketMask(((hcfConfig.getRfxcomConfig()
						.intArrayToHexStringArray(command.getMask()))));
				rfxcomJSON.setPacketOperator(hcfConfig.getRfxcomConfig()
						.operatorToStringArray(command.getOperator()));
				break;
			case GENERIC_OUTPUT:
				break;
			}

			rfxcomsJSON.getCommandList().add(rfxcomJSON);
		}

		// RfxcomCommandJSON rfxcomJSON1 = new RfxcomCommandJSON();
		// rfxcomJSON1.setName("1st one");
		// rfxcomJSON1.setDescription("Fires an event");
		// rfxcomJSON1.setEventName("Sample event on");
		// rfxcomJSON1.setRfxcomType("GENERIC INPUT");
		// rfxcomJSON1.setHysteresis(1000);
		// rfxcomJSON1.setPacketValues1(new String[] { "01", "02", "00", "A1"
		// });
		// rfxcomJSON1.setPacketValues2(new String[] { "01", "02", "00", "A2"
		// });
		// rfxcomJSON1.setPacketMask(new String[] { "FF", "FF", "00", "FF" });
		// rfxcomJSON1
		// .setPacketOperator(new String[] { "EQ", "GE", "LE", "RANGE" });
		// rfxcomsJSON.getCommandList().add(rfxcomJSON1);
		//
		// RfxcomCommandJSON rfxcomJSON2 = new RfxcomCommandJSON();
		// rfxcomJSON2.setName("2nd one");
		// rfxcomJSON2.setDescription("Sends an event");
		// rfxcomJSON2.setRfxcomType("GENERIC OUTPUT");
		// rfxcomJSON2.setHysteresis(800);
		// rfxcomJSON2.setPacketValues1(new String[] { "01", "02", "00", "A1"
		// });
		// rfxcomsJSON.getCommandList().add(rfxcomJSON2);
		//
		out.print(gson.toJson(rfxcomsJSON));
	}

}
