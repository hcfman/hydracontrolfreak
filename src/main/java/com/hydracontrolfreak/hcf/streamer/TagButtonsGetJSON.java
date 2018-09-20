package com.hydracontrolfreak.hcf.streamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hydracontrolfreak.hcf.hcfdevice.config.Action;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.TagActionImpl;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import com.hydracontrolfreak.hcf.hcfdevice.config.ActionType;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.json.TagButton;
import com.hydracontrolfreak.hcf.json.TagButtonsJSON;
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

@WebServlet(urlPatterns={"/tagbuttonsgetjson"})
public class TagButtonsGetJSON extends HttpServlet {
	private static final long serialVersionUID = 483239419320902596L;
	private static final Logger logger = Logger
			.getLogger(TagButtonsGetJSON.class);
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

		TagButtonsJSON tagButtonsJSON = new TagButtonsJSON(true);

		Map<String, TagButton> tagButtonMap = new TreeMap<String, TagButton>();
		for (Action action : hcfConfig.getActionConfig().getActionList()) {
			if (!(action instanceof TagActionImpl))
				continue;

			if (logger.isDebugEnabled())
				logger.debug("Action is ACTION_ADD_TAG");

			TagActionImpl tagActionImpl = (TagActionImpl) action;

			if (tagActionImpl.getValidFor() != null
					&& tagActionImpl.getValidFor() != 0L) {
				if (logger.isDebugEnabled())
					logger.debug("Skipping");
				continue;
			}

			TagButton tagButton = tagButtonMap.get(tagActionImpl.getTagName());
			if (tagButton == null) {
				tagButton = new TagButton();
				if (logger.isDebugEnabled())
					logger.debug("tagName is: " + tagActionImpl.getTagName());
				tagButton.setTagName(tagActionImpl.getTagName());
			}

			if (action.getActionType() == ActionType.ACTION_ADD_TAG) {
				if (logger.isDebugEnabled())
					logger.debug("Action is seton");
				tagButton.setOnEventName(action.getEventName());
			} else {
				if (logger.isDebugEnabled())
					logger.debug("Action is setoff");
				tagButton.setOffEventName(action.getEventName());
			}

			if (logger.isDebugEnabled())
				logger.debug("Putting (" + tagActionImpl.getTagName() + ", "
						+ tagButton + ")");
			tagButtonMap.put(tagActionImpl.getTagName(), tagButton);

		}

		tagButtonsJSON.setTagButtons(tagButtonMap.values());
		out.print(gson.toJson(tagButtonsJSON));
	}

}
