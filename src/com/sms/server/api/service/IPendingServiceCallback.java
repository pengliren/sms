package com.sms.server.api.service;


public interface IPendingServiceCallback {

	/**
	 * Triggered when results are recieved
	 * 
	 * @param call Call object this callback is applied to
	 */
	public void resultReceived(IPendingServiceCall call);
}
