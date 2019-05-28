package com.sms.server.exception;

import com.sms.server.api.IScope;

/**
 * Scope not found, thrown when child scope wasn't found.
 */
public class ScopeNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8512088658139018041L;

    /**
     * Create exception from given scope object and given child subscope
     * @param scope             Scope
     * @param childName         Subscope name
     */
    public ScopeNotFoundException(IScope scope, String childName) {
		super("Scope not found: " + childName + " in " + scope);
	}

}
