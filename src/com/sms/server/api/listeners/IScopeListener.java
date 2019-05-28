package com.sms.server.api.listeners;

import com.sms.server.api.IScope;

public interface IScopeListener {

	/**
	 * A scope has been created.
	 * 
	 * @param scope the new scope
	 */
	public void notifyScopeCreated(IScope scope);

	/**
	 * A scope has been removed.
	 * 
	 * @param scope the removed scope
	 */
	public void notifyScopeRemoved(IScope scope);
}
