package com.hydracontrolfreak.hcf.hcfdevice.config;

public interface PhidgetDevice {

	public String getName();

	public void setName(String name);

	public String getDescription();

	public void setDescription(String description);

	public int getSerialNumber();
	
	public int getPortSize();

	public void setSerialNumber(int serialNumber);

	public boolean[] getOutputState();

	public void setOutputState(boolean[] outputState);

	public boolean[] getInputState();

	public void setInputState(boolean[] inputState);

	public boolean[] getInitialOutputState();

	public void setInitialOutputState(boolean[] initialOutputState);
	
	public boolean[] getInitialInputState();

	public void setInitialInputState(boolean[] initialInputState);
	
	public String[] getOnTriggerEventNames();

	public String[] getOffTriggerEventNames();
	
	public boolean isConnected();
	
	public void setConnected(boolean connected);
	
}
