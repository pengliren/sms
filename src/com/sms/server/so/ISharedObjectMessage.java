package com.sms.server.so;

import java.util.Queue;

import com.sms.server.net.rtmp.event.IRTMPEvent;

/**
 * Shared object message
 */
public interface ISharedObjectMessage extends IRTMPEvent {

	/**
	 * Returns the name of the shared object this message belongs to.
	 * 
	 * @return name of the shared object
	 */
	public String getName();

	/**
	 * Returns the version to modify.
	 *  
	 * @return version to modify
	 */
	public int getVersion();

	/**
	 * Does the message affect a persistent shared object? 
	 * 
	 * @return true if a persistent shared object should be updated otherwise
	 *         false
	 */
	public boolean isPersistent();
	
	/**
	 * Returns a set of ISharedObjectEvent objects containing informations what
	 * to change.
	 *  
	 * @return set of ISharedObjectEvents
	 */
	public Queue<ISharedObjectEvent> getEvents();

    /**
     * Addition event handler
     * @param type           Event type
     * @param key            Handler key
     * @param value          Event value (like arguments)
     */
    public void addEvent(ISharedObjectEvent.Type type, String key, Object value);

    /**
     * Add event handler
     * @param event          SO event
     */
	public void addEvent(ISharedObjectEvent event);

    /**
     * Clear shared object
     */
    public void clear();

	/**
     * Is empty?
     *
     * @return  <code>true</code> if shared object is empty, <code>false</code> otherwise
     */
    public boolean isEmpty();
}
