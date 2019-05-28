package com.sms.io.mp4;

import com.sms.io.IStreamableFileService;
import com.sms.io.object.Deserializer;
import com.sms.io.object.Serializer;

/**
 * A MP4Service sets up the service and hands out MP4 objects to 
 * its callers
 */
public interface IMP4Service extends IStreamableFileService {

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
