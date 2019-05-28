package com.sms.io.mp4.impl;

import java.io.File;
import java.io.IOException;

import com.sms.io.BaseStreamableFileService;
import com.sms.io.IStreamableFile;
import com.sms.io.mp4.IMP4Service;
import com.sms.io.object.Deserializer;
import com.sms.io.object.Serializer;

/**
 * A MP4ServiceImpl sets up the service and hands out MP4 objects to 
 * its callers.
 */
public class MP4Service extends BaseStreamableFileService implements IMP4Service {

    /**
     * Serializer
     */
    private Serializer serializer;

    /**
     * Deserializer
     */
    private Deserializer deserializer;
    
    /**
     * File extensions handled by this service. If there are more than one, they
     * are comma separated. '.mp4' must be the first on the list because it is the 
     * default file extension for mp4 files. 
     * 
     * @see http://help.adobe.com/en_US/flashmediaserver/devguide/WS5b3ccc516d4fbf351e63e3d11a0773d117-7fc8.html 
     */
    private static String extension = ".mp4,.f4v,.mov,.3gp,.3g2,.m4v";
    
    private static String prefix = "mp4";
    
	/** {@inheritDoc} */
    @Override
    public void setPrefix(String prefix) {
		MP4Service.prefix = prefix;
	}    
    
	/** {@inheritDoc} */
    @Override
	public String getPrefix() {
		return prefix;
	}

	/** {@inheritDoc} */
    @Override
    public void setExtension(String extension) {
		MP4Service.extension = extension;
	}
	
	/** {@inheritDoc} */
    @Override
	public String getExtension() {
		return extension;
	}

	/** 
     * {@inheritDoc}
	 */
	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	/** {@inheritDoc}
	 */
	public void setDeserializer(Deserializer deserializer) {
		this.deserializer = deserializer;

	}

	/** {@inheritDoc}
	 */
	@Override
	public IStreamableFile getStreamableFile(File file) throws IOException {
		return new MP4(file);
	}

	/**
     * Getter for serializer
     *
     * @return  Serializer
     */
    public Serializer getSerializer() {
		return serializer;
	}

	/**
     * Getter for deserializer
     *
     * @return  Deserializer
     */
    public Deserializer getDeserializer() {
		return deserializer;
	}
}
