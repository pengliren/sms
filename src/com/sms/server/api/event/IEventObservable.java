package com.sms.server.api.event;

import java.util.Iterator;

public interface IEventObservable {

	 /**
     * Add event listener to this observable
     * @param listener      Event listener
     * @return true if listener is removed and false otherwise
     */
	public boolean addEventListener(IEventListener listener);

    /**
     * Remove event listener from this observable
     * @param listener      Event listener
     * @return true if listener is added and false otherwise
     */
    public boolean removeEventListener(IEventListener listener);

	/**
     * Iterator for event listeners
     *
     * @return  Event listeners iterator
     */
    public Iterator<IEventListener> getEventListeners();
}
