package com.sms.server.stream;

import com.sms.server.api.event.IEvent;

/**
 * Source for streams
 */
public interface IStreamSource {
    /**
     * Is there something more to stream?
     * @return      <code>true</code> if there's streamable data, <code>false</code> otherwise
     */
	public abstract boolean hasMore();

    /**
     * Double ended queue of event objects
     * @return      Event from queue
     */
    public abstract IEvent dequeue();

    /**
     * Close stream source
     */
	public abstract void close();

}
