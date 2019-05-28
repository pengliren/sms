package com.sms.server.api;

import com.sms.server.exception.ScopeNotFoundException;

public interface IScopeResolver {

	/**
	 * Return the global scope.
	 * 
	 * @return      Global scope
	 */
	public IGlobalScope getGlobalScope();

	/**
	 * Get the scope for a given path.
	 * 
	 * @param path Path to return the scope for
	 * @return      Scope for passed path
	 * @throws ScopeNotFoundException If scope doesn't exist an can't be created
	 */
	public IScope resolveScope(String path);

	/**
	 * Get the scope for a given path from a root scope.
	 * 
	 * @param root The scope to start traversing from.
	 * @param path Path to return the scope for.
	 * @return		Scope for passed path.
	 */
	public IScope resolveScope(IScope root, String path);
}
