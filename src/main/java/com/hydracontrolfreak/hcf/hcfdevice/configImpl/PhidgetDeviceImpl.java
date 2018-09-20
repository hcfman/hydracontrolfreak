package com.hydracontrolfreak.hcf.hcfdevice.configImpl;

import com.hydracontrolfreak.hcf.hcfdevice.config.PhidgetDevice;

import java.util.Arrays;

import static com.hydracontrolfreak.hcf.hcfdevice.configImpl.PhidgetConstants.PHIDGET_PORT_SIZE;

public class PhidgetDeviceImpl implements PhidgetDevice {
	private String name;
	private String description;
	private int serialNumber;
	private int portSize;
	private boolean outputState[] = new boolean[PHIDGET_PORT_SIZE];
	private boolean inputState[] = new boolean[PHIDGET_PORT_SIZE];
	private boolean initialInputState[] = new boolean[PHIDGET_PORT_SIZE];
	private boolean initialOutputState[] = new boolean[PHIDGET_PORT_SIZE];
	private String onTriggerEventNames[] = new String[PHIDGET_PORT_SIZE];
	private String offTriggerEventNames[] = new String[PHIDGET_PORT_SIZE];
	private volatile boolean connected = false;

	public PhidgetDeviceImpl() {
	}

	public PhidgetDeviceImpl(String name, String description,
                             int serialNumber, int portSize) {
		this.name = name;
		this.description = description;
		this.serialNumber = serialNumber;
		this.portSize = portSize;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}

	public int getPortSize() {
		return portSize;
	}

	public void setPortSize(int portSize) {
		this.portSize = portSize;
	}

	public boolean[] getOutputState() {
		return outputState;
	}

	public void setOutputState(boolean[] outputState) {
		this.outputState = outputState;
	}

	public boolean[] getInputState() {
		return inputState;
	}

	public void setInputState(boolean[] inputState) {
		this.inputState = inputState;
	}

	public boolean[] getInitialOutputState() {
		return initialOutputState;
	}

	public void setInitialOutputState(boolean[] initialOutputState) {
		this.initialOutputState = initialOutputState;
	}

	public boolean[] getInitialInputState() {
		return initialInputState;
	}

	public void setInitialInputState(boolean[] initialInputState) {
		this.initialInputState = initialInputState;
	}

	public String[] getOnTriggerEventNames() {
		return onTriggerEventNames;
	}

	public String[] getOffTriggerEventNames() {
		return offTriggerEventNames;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	@Override
	public String toString() {
		return "PhidgetDeviceImpl [name=" + name + ", description="
				+ description + ", serialNumber=" + serialNumber
				+ ", portSize=" + portSize + ", outputState="
				+ Arrays.toString(outputState) + ", inputState="
				+ Arrays.toString(inputState) + ", initialInputState="
				+ Arrays.toString(initialInputState) + ", initialOutputState="
				+ Arrays.toString(initialOutputState)
				+ ", onTriggerEventNames="
				+ Arrays.toString(onTriggerEventNames)
				+ ", offTriggerEventNames="
				+ Arrays.toString(offTriggerEventNames) + ", connected="
				+ connected + "]";
	}

}
