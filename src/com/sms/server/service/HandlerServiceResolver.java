package com.sms.server.service;

import com.sms.server.api.IScope;
import com.sms.server.api.IScopeHandler;
import com.sms.server.api.service.IServiceHandlerProvider;

public class HandlerServiceResolver implements IServiceResolver {

	/** {@inheritDoc} */
    public Object resolveService(IScope scope, String serviceName) {
		IScopeHandler handler = scope.getHandler();
		if (handler instanceof IServiceHandlerProvider) {
			// TODO: deprecate this?
			Object result = ((IServiceHandlerProvider) handler)
					.getServiceHandler(serviceName);
			if (result != null) {
				return result;
			}
		}
		/*
		if (handler instanceof IServiceHandlerProviderAware) {
			IServiceHandlerProvider shp = ((IServiceHandlerProviderAware) handler)
					.getServiceHandlerProvider();
			if (shp != null) {
				return shp.getServiceHandler(serviceName);
			}
		}*/

		return null;
	}
}
