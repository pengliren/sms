package com.sms.server.stream;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Stream data packet
 */
public interface IStreamData<T> {

	/**
     * Getter for property 'data'.
     *
     * @return Value for property 'data'.
     */
    public IoBuffer getData();
    
    /**
     * Creates a byte accurate copy.
     * 
     * @return duplicate of the current data item
     * @throws IOException
     * @throws ClassNotFoundException
     */
	public IStreamData<T> duplicate() throws IOException, ClassNotFoundException;
	
}
