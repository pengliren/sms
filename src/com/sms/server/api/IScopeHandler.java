package com.sms.server.api;

import com.sms.server.api.event.IEventHandler;
import com.sms.server.api.service.IServiceCall;

public interface IScopeHandler extends IEventHandler {

	/**
	 * Called when a scope is created for the first time.
	 * 
	 * @param scope
	 * 			the new scope object
	 * @return <code>true</code> to allow, <code>false</code> to deny
	 */
	boolean start(IScope scope);

	/**
	 * Called just before a scope is disposed.
     * @param scope         Scope that id disposed
     */
	void stop(IScope scope);

	/**
	 * Called just before every connection to a scope. You can pass additional
	 * params from client using <code>NetConnection.connect</code> method (see
	 * below).
	 * 
	 * @param conn
	 * 			Connection object
	 * @param params
	 *            List of params passed from client via
	 *            <code>NetConnection.connect</code> method. All parameters
	 *            but the first one passed to <code>NetConnection.connect</code>
	 *            method are available as params array.
	 * 
	 * 
	 * @return <code>true</code> to allow, <code>false</code> to deny
     * @param scope           Scope object
	 */
	boolean connect(IConnection conn, IScope scope, Object[] params);

	/**
	 * Called just after the a connection is disconnected.
	 * 
	 * @param conn
	 * 			Connection object
	 * @param scope
	 * 			Scope object
	 */
	void disconnect(IConnection conn, IScope scope);

	/**
	 * Called just before a child scope is added.
	 * 
	 * @param scope
	 * 			Scope that will be added
	 * @return <code>true</code> to allow, <code>false</code> to deny
	 */
	boolean addChildScope(IBasicScope scope);

	/**
	 * Called just after a child scope has been removed.
	 * 
	 * @param scope
	 * 			Scope that has been removed
	 */
	void removeChildScope(IBasicScope scope);

	/**
	 * Called just before a client enters the scope.
	 * 
	 * @param client
	 * 			Client object
	 * @return <code>true</code> to allow, <code>false</code> to deny
	 *         connection
     * @param scope      Scope that is joined by client
	 */
	boolean join(IClient client, IScope scope);

	/**
	 * Called just after the client leaves the scope.
	 * 
	 * @param client
	 * 			Client object
	 * @param scope
	 * 			Scope object
	 */
	void leave(IClient client, IScope scope);

	/**
	 * Called when a service is called.
	 * 
	 * @param conn
	 * 			The connection object
	 * @param call
	 * 			The call object.
	 * 
	 * @return <code>true</code> to allow, <code>false</code> to deny
	 */
	boolean serviceCall(IConnection conn, IServiceCall call);
}
