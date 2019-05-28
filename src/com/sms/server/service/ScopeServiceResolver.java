package com.sms.server.service;

import com.sms.server.api.IScope;

public class ScopeServiceResolver implements IServiceResolver {

	/** {@inheritDoc} */
    public Object resolveService(IScope scope, String serviceName) {
		return scope.getServiceHandler(serviceName);
	}
}
