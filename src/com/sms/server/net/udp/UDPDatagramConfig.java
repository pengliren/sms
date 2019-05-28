package com.sms.server.net.udp;

import org.apache.mina.transport.socket.DefaultDatagramSessionConfig;

/**
 * UDP DatagramConfig
 * @author pengliren
 *
 */
public class UDPDatagramConfig extends DefaultDatagramSessionConfig {

	private int multicastTimeout = 250;
	private boolean multicastBindToAddress = true;
	private int datagramMaximumPacketSize = 8192;
	private String multicastInterfaceAddress;

	public UDPDatagramConfig() {

	}
	
	public int getMulticastTimeout() {
		return this.multicastTimeout;
	}

	public void setMulticastTimeout(int multicastTimeout) {
		this.multicastTimeout = multicastTimeout;
	}

	public boolean isMulticastBindToAddress() {
		return this.multicastBindToAddress;
	}

	public void setMulticastBindToAddress(boolean multicastBindToAddress) {
		this.multicastBindToAddress = multicastBindToAddress;
	}

	public int getDatagramMaximumPacketSize() {
		return this.datagramMaximumPacketSize;
	}

	public void setDatagramMaximumPacketSize(int datagramMaximumPacketSize) {
		this.datagramMaximumPacketSize = datagramMaximumPacketSize;
	}

	public String getMulticastInterfaceAddress() {
		return this.multicastInterfaceAddress;
	}

	public void setMulticastInterfaceAddress(String multicastInterfaceAddress) {
		this.multicastInterfaceAddress = multicastInterfaceAddress;
	}
}
