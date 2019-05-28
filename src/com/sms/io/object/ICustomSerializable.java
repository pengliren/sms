package com.sms.io.object;

/**
 * Interface for objects that know how to serialize their contents.
 * 
 * NOTE: This is only used for AMF0 encoding and you should not need to
 * implement this in your own objects.
 */
public interface ICustomSerializable {

	/**
	 * Serialize this object to the given output stream.
	 * 
	 * @param output output
	 */
	public void serialize(Output output);
	
}
