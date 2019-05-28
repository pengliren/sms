package com.sms.server.net.rtmp.event;

import com.sms.server.api.event.IEvent;
import com.sms.server.api.event.IEventListener;
import com.sms.server.net.rtmp.message.Header;

public interface IRTMPEvent extends IEvent {

	/**
     * Getter for data type
     *
     * @return  Data type
     */
    public byte getDataType();

	/**
     * Setter for source
     *
     * @param source Source
     */
    public void setSource(IEventListener source);

	/**
     * Getter for header
     *
     * @return  RTMP packet header
     */
    public Header getHeader();

	/**
     * Setter for header
     *
     * @param header RTMP packet header
     */
    public void setHeader(Header header);

	/**
     * Getter for timestamp
     *
     * @return  Event timestamp
     */
    public long getTimestamp();

	/**
     * Setter for timestamp
     *
     * @param timestamp  New event timestamp
     */
    public void setTimestamp(long timestamp);
    
	/**
     * Getter for source type
     *
     * @return  Source type
     */
    public byte getSourceType();    

	/**
     * Setter for source type
     *
     * @param sourceType 
     */
    public void setSourceType(byte sourceType);
        
    /**
     * Retain event
     */
    public void retain();

	/**
	 * Hook to free buffers allocated by the event.
	 */
	public void release();

}
