package com.hydracontrolfreak.hcf.streamer;

import com.hydracontrolfreak.hcf.eventlib.ShutdownEvent;
import com.hydracontrolfreak.hcf.freak.Freak;
import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@Configuration
public class ContextListener implements ServletContextListener {
	ServletContext context;
	FreakApi freak;

	public void contextInitialized(ServletContextEvent contextEvent) {
		if (freak == null)
			freak = Freak.getInstance();

		freak.start();
	}

	public void contextDestroyed(ServletContextEvent contextEvent) {
		context = contextEvent.getServletContext();

		if (freak == null)
			freak = Freak.getInstance();
		if (!freak.isReady())
			return;
		
		freak.sendEvent(new ShutdownEvent());
	}

}
