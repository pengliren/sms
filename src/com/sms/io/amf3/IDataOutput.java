package com.sms.io.amf3;

import java.nio.ByteOrder;

/**
 * Interface implemented by classes that provide a way to store custom objects.
 * 
 * @see IExternalizable#writeExternal(IDataOutput)
 * @see <a href="http://livedocs.adobe.com/flex/2/langref/flash/utils/IDataOutput.html">Adobe Livedocs (external)</a>
 */
public interface IDataOutput {

	/**
	 * Return the byteorder used when storing values.
	 * 
	 * @return the byteorder
	 */
	public ByteOrder getEndian();
	
	/**
	 * Set the byteorder to use when storing values.
	 * 
	 * @param endian the byteorder to use
	 */
	public void setEndian(ByteOrder endian);
	
	/**
	 * Write boolean value.
	 * 
	 * @param value the value
	 */
	public void writeBoolean(boolean value);
	
	/**
	 * Write signed byte value.
	 * 
	 * @param value the value
	 */
	public void writeByte(byte value);
	
	/**
	 * Write multiple bytes.
	 *  
	 * @param bytes the bytes
	 */
	public void writeBytes(byte[] bytes);
	
	/**
	 * Write multiple bytes from given offset.
	 * 
	 * @param bytes the bytes
	 * @param offset offset in bytes to start writing from
	 */
	public void writeBytes(byte[] bytes, int offset);
	
	/**
	 * Write given number of bytes from given offset.
	 * 
	 * @param bytes the bytes
	 * @param offset offset in bytes to start writing from
	 * @param length number of bytes to write
	 */
	public void writeBytes(byte[] bytes, int offset, int length);
	
	/**
	 * Write double-precision floating point value.
	 * 
	 * @param value the value
	 */
	public void writeDouble(double value);
	
	/**
	 * Write single-precision floating point value.
	 * 
	 * @param value the value
	 */
	public void writeFloat(float value);
	
	/**
	 * Write signed integer value.
	 * 
	 * @param value the value
	 */
	public void writeInt(int value);
	
	/**
	 * Write string in given character set.
	 * 
	 * @param value the string
	 * @param encoding the character set
	 */
	public void writeMultiByte(String value, String encoding);
	
	/**
	 * Write arbitrary object.
	 * 
	 * @param value the object
	 */
	public void writeObject(Object value);
	
	/**
	 * Write signed short value.
	 * 
	 * @param value the value
	 */
	public void writeShort(short value);
	
	/**
	 * Write unsigned integer value.
	 * 
	 * @param value the value
	 */
	public void writeUnsignedInt(long value);
	
	/**
	 * Write UTF-8 encoded string.
	 * 
	 * @param value the string
	 */
	public void writeUTF(String value);
	
	/**
	 * Write UTF-8 encoded string as byte array. This string is stored without informations
	 * about its length, so {@link IDataInput#readUTFBytes(int)} must be used to load it.
	 * 
	 * @param value the string
	 */
	public void writeUTFBytes(String value);

}
