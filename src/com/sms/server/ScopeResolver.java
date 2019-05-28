package com.sms.server;

import com.sms.server.api.IGlobalScope;
import com.sms.server.api.IScope;
import com.sms.server.api.IScopeResolver;
import com.sms.server.exception.ScopeNotFoundException;
import com.sms.server.exception.ScopeShuttingDownException;

public class ScopeResolver implements IScopeResolver {

	/**
     *  Default host constant
     */
	public static final String DEFAULT_HOST = "";
	
    /**
     *  Global scope
     */
	protected IGlobalScope globalScope;

    /**
     * Getter for global scope
     * @return      Global scope
     */
	public IGlobalScope getGlobalScope() {
		return globalScope;  
	}

    /**
     * Setter for global scope
     * @param root        Global scope
     */
	public void setGlobalScope(IGlobalScope root) {
		this.globalScope = root;
	}

    /**
     * Return scope associated with given path
     *
     * @param path        Scope path
     * @return            Scope object
     */
	public IScope resolveScope(String path) {
        // Start from global scope
		return resolveScope(globalScope, path);
	}

    /**
     * Return scope associated with given path from given root scope.
     *
     * @param root        Scope to start from
     * @param path        Scope path
     * @return            Scope object
     */
	public IScope resolveScope(IScope root, String path) {
        // Start from root scope
        IScope scope = root;
        // If there's no path return root scope (e.i. root path scope)
        if (path != null || !"".equals(path)) {
            // Split path to parts
            final String[] parts = path.split("/");
            // Iterate thru them, skip empty parts
    		for (String room : parts) {
    			if (room == null || "".equals(room)) {
    				// Skip empty path elements
    				continue;
    			}
    
    			if (scope.hasChildScope(room)) {
    				scope = scope.getScope(room);
    			} else if (!scope.equals(root)) {
    				//no need for sync here, scope.children is concurrent
    				if (scope.createChildScope(room)) {
    					scope = scope.getScope(room);
    				}
    			} 
    			
    			//if the scope is still equal to root then the room was not found
    			if (scope == root) {
    				throw new ScopeNotFoundException(scope, room);
    			}
    			
    			if (scope instanceof WebScope && ((WebScope) scope).isShuttingDown()) {
    				throw new ScopeShuttingDownException(scope);
    			}
    		}
		}
		return scope;
	}
}
