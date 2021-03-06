package com.hydracontrolfreak.hcf.hcfdevice.config;

import com.hydracontrolfreak.hcf.timeRanges.TimeSpec;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ScheduleConfig {
	private static final Logger logger = Logger.getLogger(ScheduleConfig.class);
	private volatile List<Schedule> schedules = new ArrayList<Schedule>();

	public List<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}

	public void addScheduleConfig(Document doc, Element parent) {
		Element schedulesElement = doc.createElement("schedules");

		for (Schedule schedule : schedules) {
			schedulesElement.appendChild(doc.createTextNode("\n\t"));
			if (logger.isDebugEnabled()) logger.debug("Schedule maker");
			Element scheduleElement = doc.createElement("schedule");
			schedulesElement.appendChild(scheduleElement);
			scheduleElement.appendChild(doc.createTextNode("\n\t\t"));
			scheduleElement.setAttribute("name", schedule.getName());

			Element eventNameElement = doc.createElement("eventName");
			eventNameElement.setTextContent(schedule.getEventName());
			scheduleElement.appendChild(eventNameElement);
			scheduleElement.appendChild(doc.createTextNode("\n\t\t"));
			
			Element timeSpecsElement = doc.createElement("timespecs");
			scheduleElement.appendChild(timeSpecsElement);
			timeSpecsElement.appendChild(doc.createTextNode("\n\t\t\t"));

			TimeSpec timeSpec = schedule.getTimeSpec();
			Element timeSpecElement = TimeSpecToXML.toXML(doc, timeSpec, 2);
			timeSpecsElement.appendChild(timeSpecElement);
			timeSpecsElement.appendChild(doc.createTextNode("\n\t\t"));
			
			scheduleElement.appendChild(doc.createTextNode("\n\t"));

			schedulesElement.appendChild(doc.createTextNode("\n"));
		}
		parent.appendChild(schedulesElement);
		parent.appendChild(doc.createTextNode("\n\n"));
	}

	@Override
	public String toString() {
		return "ScheduleConfig [schedules=" + schedules + "]";
	}

}
