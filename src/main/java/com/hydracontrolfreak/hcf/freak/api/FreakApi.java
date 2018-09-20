package com.hydracontrolfreak.hcf.freak.api;

import com.hydracontrolfreak.hcf.eventlib.Event;
import com.hydracontrolfreak.hcf.eventlib.EventListener;
import com.hydracontrolfreak.hcf.hcfdevice.config.HcfDeviceConfig;
import com.hydracontrolfreak.hcf.rfxcomhandler.RfxcomHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public interface FreakApi {

	public void start();

	public boolean isReady();

	public void sendEvent(Event event);

	public void sendEmailEvent(Event event);

	public void sendRfxcomEvent(Event event);

	public void sendDvrEvent(Event event);

	public void sendHttpEvent(Event event);

	public void sendPhidgetEvent(Event event);

	public List<String> getTagList();

	public void clearTags(List<String> tagList);

	public AtomicBoolean getUpdating();

	public HcfDeviceConfig getHcfConfig();

	public boolean saveConfig();

	public String getHcfBase();

	public boolean mountReadonly();

	public boolean mountReadWrite();

	public void subscribeEvents(EventListener listener, String subscription);

	public Map<EventListener, Pattern> getListenerMap();

	public void unsubscribeEvents(EventListener listener);

	public RfxcomHandler getRfxcomHandler();
};
