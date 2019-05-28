package com.sms.io.m4a.impl;

import java.io.File;
import java.io.IOException;

import com.sms.io.BaseStreamableFileService;
import com.sms.io.IStreamableFile;
import com.sms.io.m4a.IM4AService;
import com.sms.io.object.Deserializer;
import com.sms.io.object.Serializer;

/**
 * A M4AServiceImpl sets up the service and hands out M4A objects to 
 * its callers.
 */
public class M4AService extends BaseStreamableFileService implements IM4AService {

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
     * are comma separated.
     */
    private static String extension = ".f4a,.m4a,.aac";
    
    private static String prefix = "f4a";
    
	/** {@inheritDoc} */
    @Override
    public void setPrefix(String prefix) {
    	M4AService.prefix = prefix;
	}    
    
	/** {@inheritDoc} */
    @Override
	public String getPrefix() {
		return prefix;
	}

	/** {@inheritDoc} */
    @Override
    public void setExtension(String extension) {
    	M4AService.extension = extension;
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
		return new M4A(file);
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
