package com.sms.server.api.service;

import com.sms.server.api.IScope;

public interface IServiceInvoker {

	/**
	 * Execute the passed service call in the given scope.  This looks up the
	 * handler for the call in the scope and the context of the scope.
	 * 
	 * @param call
	 * 			the call to invoke
	 * @param scope
	 * 			the scope to search for a handler
	 * @return <code>true</code> if the call was performed, otherwise <code>false</code>
	 */
    boolean invoke(IServiceCall call, IScope scope);

	/**
	 * Execute the passed service call in the given object.
	 * 
	 * @param call
	 * 			the call to invoke
	 * @param service
	 * 			the service to use
	 * @return <code>true</code> if the call was performed, otherwise <code>false</code>
	 */
    boolean invoke(IServiceCall call, Object service);
}
