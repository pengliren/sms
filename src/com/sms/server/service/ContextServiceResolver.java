package com.sms.server.service;

import com.sms.server.api.IScope;

public class ContextServiceResolver implements IServiceResolver {

	/** {@inheritDoc} */
    public Object resolveService(IScope scope, String serviceName) {
		Object service = null;
		try {
			service = scope.getContext().getService(serviceName);
		} catch (ServiceNotFoundException err) {
			return null;
		}
		if (service != null) {
			return service;
		}

		return null;
	}
}
