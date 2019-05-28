package com.sms.server.exception;

import com.sms.server.api.IScope;

/**
 * Scope is currently shutting down.
 */
public class ScopeShuttingDownException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9129189610425512289L;

    /**
     * Create exception from given scope object
     * @param scope             Scope
     */
    public ScopeShuttingDownException(IScope scope) {
		super("Scope shutting down: " + scope);
	}

}
