package com.sms.server.api.so;

import com.sms.server.api.IConnection;

/**
 * Clientside access to shared objects.
 *
 */
public interface IClientSharedObject extends ISharedObjectBase {

	/**
	 * Connect the shared object using the passed connection.
	 * 
	 * @param conn connect to connect to
	 */
	public void connect(IConnection conn);
	
	/**
	 * Check if the shared object is connected to the server.
	 * 
	 * @return is connected
	 */
	public boolean isConnected();
	
	/**
	 * Disconnect the shared object.
	 */
	public void disconnect();
}
