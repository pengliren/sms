package com.sms.server.api.stream;


import org.apache.mina.core.buffer.IoBuffer;

/**
 * Represents an Audio codec and its associated decoder configuration.
 */
public interface IAudioStreamCodec {
	
	/**
	 * @return the name of the audio codec.
     */
	public String getName();

	/**
	 * Reset the codec to its initial state.
	 */
	public void reset();

	/**
	 * Returns true if the codec knows how to handle the passed
	 * stream data.
     * @param data some sample data to see if this codec can handle it.
     * @return can this code handle the data.
     */
	public boolean canHandleData(IoBuffer data);

	/**
	 * Update the state of the codec with the passed data.
     * @param data data to tell the codec we're adding
     * @return true for success. false for error.
     */
	public boolean addData(IoBuffer data);
	
	/**
	 * Returns information used to configure the decoder.
	 * 
	 * @return the data for decoder setup.
     */
	public IoBuffer getDecoderConfiguration();
	
}
