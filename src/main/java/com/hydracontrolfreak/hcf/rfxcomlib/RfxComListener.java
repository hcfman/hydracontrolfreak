package com.hydracontrolfreak.hcf.rfxcomlib;

public interface RfxComListener {
	public void fire(Packet packet);
	
	public RfxcomCommand getCommand();
	
	public boolean matches(Packet packet);
}
