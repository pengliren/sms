package com.sms.server.adapter;

import java.util.Map;

import com.sms.server.api.IBasicScope;
import com.sms.server.api.IClient;
import com.sms.server.api.IConnection;
import com.sms.server.api.IScope;
import com.sms.server.api.IScopeHandler;
import com.sms.server.api.SMS;
import com.sms.server.api.event.IEvent;
import com.sms.server.api.service.IServiceCall;

public abstract class AbstractScopeAdapter implements IScopeHandler {

	/**
     * Can start flag.
     * <code>true</code> if scope is ready to be activated, <code>false</code> otherwise
     */
	private boolean canStart = true;
    /**
     * Can connect flag.
     * <code>true</code> if connections to scope are allowed, <code>false</code> otherwise
     */
	private boolean canConnect = true;
    /**
     * Can join flag.
     * <code>true</code> if scope may be joined by users, <code>false</code> otherwise
     */
	private boolean canJoin = true;
    /**
     * Can call service flag.
     * <code>true</code> if remote service calls are allowed for the scope, <code>false</code> otherwise
     */
	private boolean canCallService = true;
    /**
     * Can add child scope flag. <code>true</code> if scope is allowed to add child scopes, <code>false</code> otherwise
     */
	private boolean canAddChildScope = true;

    /**
     * Can handle event flag.
     * <code>true</code> if events handling is allowed, <code>false</code> otherwise
     */
    private boolean canHandleEvent = true;

	/**
     * Setter for can start flag.
     *
     * @param canStart  <code>true</code> if scope is ready to be activated, <code>false</code> otherwise
     */
    public void setCanStart(boolean canStart) {
		this.canStart = canStart;
	}

	/**
     * Setter for can call service flag
     *
     * @param canCallService <code>true</code> if remote service calls are allowed for the scope, <code>false</code> otherwise
     */
    public void setCanCallService(boolean canCallService) {
		this.canCallService = canCallService;
	}

	/**
     * Setter for can connect flag
     *
     * @param canConnect <code>true</code> if connections to scope are allowed, <code>false</code> otherwise
     */
    public void setCanConnect(boolean canConnect) {
		this.canConnect = canConnect;
	}

	/**
     * Setter for 'can join' flag
     *
     * @param canJoin <code>true</code> if scope may be joined by users, <code>false</code> otherwise
     */
    public void setJoin(boolean canJoin) {
		this.canJoin = canJoin;
	}

	/** {@inheritDoc} */
    public boolean start(IScope scope) {
		return canStart;
	}

	/** {@inheritDoc} */
    public void stop(IScope scope) {
		// nothing
	}

	/** {@inheritDoc} */
    public boolean connect(IConnection conn, IScope scope, Object[] params) {
		return canConnect;
	}

	/** {@inheritDoc} */
    public void disconnect(IConnection conn, IScope scope) {
		// nothing
	}

	/** {@inheritDoc} */
    public boolean join(IClient client, IScope scope) {
		return canJoin;
	}

	/** {@inheritDoc} */
    public void leave(IClient client, IScope scope) {
		// nothing
	}

	/** {@inheritDoc} */
    public boolean serviceCall(IConnection conn, IServiceCall call) {
		return canCallService;
	}

	/** {@inheritDoc} */
    public boolean addChildScope(IBasicScope scope) {
		return canAddChildScope;
	}

	/** {@inheritDoc} */
    public void removeChildScope(IBasicScope scope) {
	}

	/** {@inheritDoc} */
    public boolean handleEvent(IEvent event) {
		return canHandleEvent;
	}
    
	/**
	 * Calls the checkBandwidth method on the current client.
	 * @param o Object passed from Flash, not used at the moment 
	 */
    public void checkBandwidth(Object o) {
    	//Incoming object should be null
    	IClient client = SMS.getConnectionLocal().getClient();
    	if (client != null) {
    		client.checkBandwidth();
    	}
    }
    
    /**
	 * Calls the checkBandwidthUp method on the current client.
	 * @param params Object passed from Flash
	 */
    public Map<String, Object> checkBandwidthUp(Object[] params) {
    	//Incoming object should be null
    	IClient client = SMS.getConnectionLocal().getClient();
    	if (client != null) {
    		return client.checkBandwidthUp(params);
    	}
		return null;
    }
}
