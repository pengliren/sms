package com.sms.io;

import java.io.File;
import java.util.Set;

import com.sms.server.api.IScopeService;

/**
 * Scope service extension that provides method to get streamable file services set
 */
public interface IStreamableFileFactory extends IScopeService {

	public static String BEAN_NAME = "streamableFileFactory";

	public abstract IStreamableFileService getService(File fp);

	/**
     * Getter for services
     *
     * @return  Set of streamable file services
     */
    public abstract Set<IStreamableFileService> getServices();

}