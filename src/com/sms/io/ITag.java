package com.sms.io;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * A Tag represents the contents or payload of a streamable file.
 * 
 */
public interface ITag extends IoConstants {

	/**
	 * Return the body ByteBuffer
	 * 
	 * @return ByteBuffer        Body as byte buffer
	 */
	public IoBuffer getBody();

	/**
	 * Return the size of the body
	 * 
	 * @return int               Body size
	 */
	public int getBodySize();

	/**
	 * Get the data type
	 * 
	 * @return byte              Data type as byte
	 */
	public byte getDataType();

	/**
	 * Return the timestamp
	 * 
	 * @return int               Timestamp
	 */
	public int getTimestamp();

	/**
	 * Returns the data as a ByteBuffer
	 * 
	 * @return ByteBuffer        Data as byte buffer
	 */
	public IoBuffer getData();

	/**
	 * Returns previous tag size
	 * 
	 * @return int               Previous tag size
	 */
	public int getPreviousTagSize();

	/**
	 * Set the body ByteBuffer.
     * @param body               Body as ByteBuffer
     */
	public void setBody(IoBuffer body);

	/**
	 * Set the size of the body.
     * @param size               Body size
     */
	public void setBodySize(int size);

	/**
	 * Set the data type.
     * @param datatype           Data type
     */
	public void setDataType(byte datatype);

	/**
	 * Set the timestamp.
     * @param timestamp          Timestamp
     */
	public void setTimestamp(int timestamp);

	/**
	 * Set the size of the previous tag.
     * @param size               Previous tag size
     */
	public void setPreviousTagSize(int size);

}
