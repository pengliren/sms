package com.sms.server.net.udp;

import java.net.InetSocketAddress;

/**
 * UDP Transport Connection (abstract class)
 * @author pengliren
 *
 */
public abstract class UDPTransportConnectionBase {
	
	protected boolean isMulticast = false;
	protected InetSocketAddress address = null;
	protected boolean isOpen = true;

	public boolean isMulticast() {
		return this.isMulticast;
	}

	public InetSocketAddress getAddress() {
		return this.address;
	}

	public boolean isOpen() {
		synchronized (this) {
			return this.isOpen;
		}
	}
}
