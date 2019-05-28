package com.sms.io.m4a;

import com.sms.io.IStreamableFileService;
import com.sms.io.object.Deserializer;
import com.sms.io.object.Serializer;

/**
 * A M4AService sets up the service and hands out M4A objects to 
 * its callers
 */
public interface IM4AService extends IStreamableFileService {

	/**
	 * Sets the serializer
	 * 
	 * @param serializer        Serializer object
	 */
	public void setSerializer(Serializer serializer);

	/**
	 * Sets the deserializer
	 * 
	 * @param deserializer      Deserializer object
	 */
	public void setDeserializer(Deserializer deserializer);

}
