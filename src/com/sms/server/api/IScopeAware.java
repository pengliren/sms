package com.sms.server.api;

public interface IScopeAware {

	/**
	 * Set the scope the object is located in.
	 * 
	 * @param scope
	 *            Scope for this object
	 */
	public void setScope(IScope scope);
}
