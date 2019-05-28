package com.sms.server.adapter;

import com.sms.server.api.IClient;
import com.sms.server.api.IConnection;
import com.sms.server.api.IScope;

public interface IApplication {

	/**
	 * Called once when application or room starts
	 * 
	 * @param app Application or room level scope. See
	 *            {@link org.red5.server.api.IScope} for details
	 * @return <code>true</code> continues application run, <code>false</code>
	 *         terminates
	 */
	public boolean appStart(IScope app);

	/**
	 * Called per each client connect
	 * 
	 * @param conn Connection object used to provide basic connection methods.
	 *            See {@link org.red5.server.api.IConnection}
	 * @param params List of params sent from client with NetConnection.connect
	 *            call
	 * @return <code>true</code> accepts the connection, <code>false</code>
	 *         rejects it
	 */
	public boolean appConnect(IConnection conn, Object[] params);

	/**
	 * Called every time client joins app level scope
	 * 
	 * @param client Client object
	 * @param app Scope object
	 * @return <code>true</code> accepts the client, <code>false</code>
	 *         rejects it
	 */
	public boolean appJoin(IClient client, IScope app);

	/**
	 * Called every time client disconnects from the application
	 * 
	 * @param conn Connection object See {@link org.red5.server.api.IConnection}
	 */
	public void appDisconnect(IConnection conn);

	/**
	 * Called every time client leaves the application scope
	 * 
	 * @param client Client object
	 * @param app Scope object
	 */
	public void appLeave(IClient client, IScope app);

	/**
	 * Called on application stop
	 * 
	 * @param app Scope object
	 */
	public void appStop(IScope app);

	/**
	 * Called on application room start
	 * 
	 * @param room Scope object
	 * @return <code>true</code> if scope can be started, <code>false</code>
	 *         otherwise
	 */
	public boolean roomStart(IScope room);

	/**
	 * Called every time client connects to the room
	 * 
	 * @param conn Connection object
	 * @param params List of params sent from client with NetConnection.connect
	 *            call
	 * @return <code>true</code> accepts the connection, <code>false</code>
	 *         rejects it
	 */
	public boolean roomConnect(IConnection conn, Object[] params);

	/**
	 * Called when user joins room scope
	 * 
	 * @param client Client object
	 * @param room Scope object
	 * @return <code>true</code> accepts the client, <code>false</code>
	 *         rejects it
	 */
	public boolean roomJoin(IClient client, IScope room);

	/**
	 * Called when client disconnects from room  scope
	 * 
	 * @param conn Connection object used to provide basic connection methods.
	 *            See {@link org.red5.server.api.IConnection}
	 */
	public void roomDisconnect(IConnection conn);

	/**
	 * Called when user leaves room scope
	 * 
	 * @param client Client object
	 * @param room Scope object
	 */
	public void roomLeave(IClient client, IScope room);

	/**
	 * Called on room scope stop
	 * 
	 * @param room Scope object
	 */
	public void roomStop(IScope room);
}
