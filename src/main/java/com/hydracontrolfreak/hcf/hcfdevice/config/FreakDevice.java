package com.hydracontrolfreak.hcf.hcfdevice.config;

import com.hydracontrolfreak.hcf.hcfdevice.configImpl.ProtocolType;
import com.hydracontrolfreak.hcf.hcfdevice.configImpl.ProtocolType;


public interface FreakDevice {
	public String getHostname();

	public void setHostname(String hostname);

	public int getPort();

	public void setPort(int port);
	
	public String getUsername();
	
	public void setUsername(String username);
	
	public String getPassword();
	
	public void setPassword(String password);

	public String getName();

	public void setName(String name);

	public String getDescription();

	public void setDescription(String description);
	
	public ProtocolType getProtocol();

	public void setProtocol(ProtocolType protocol);
	
	public boolean isVerifyHostname();

	public void setVerifyHostname(boolean verifyHostname);
	
	public boolean isGuest();

	public void setGuest(boolean guest);
	
}
