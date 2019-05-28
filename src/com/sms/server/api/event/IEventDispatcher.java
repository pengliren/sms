package com.sms.server.api.event;


public interface IEventDispatcher {

	/**
	 * Dispatches event
	 * @param event	Event object
	 */
	public void dispatchEvent(IEvent event);
}
