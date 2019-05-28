package com.sms.server.api.listeners;

import com.sms.server.api.IConnection;

public interface IConnectionListener {

	/**
	 * A new connection was established.
	 * 
	 * @param conn the new connection
	 */
	public void notifyConnected(IConnection conn);

	/**
	 * A connection was disconnected.
	 * 
	 * @param conn the disconnected connection
	 */
	public void notifyDisconnected(IConnection conn);
}
