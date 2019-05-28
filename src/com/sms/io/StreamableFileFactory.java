package com.sms.io;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.flv.impl.FLVService;
import com.sms.io.m4a.impl.M4AService;
import com.sms.io.mp3.impl.MP3Service;
import com.sms.io.mp4.impl.MP4Service;

/**
 * Creates streamable file services
 */
public class StreamableFileFactory implements IStreamableFileFactory {

	// Initialize Logging
	public static Logger logger = LoggerFactory.getLogger(StreamableFileFactory.class);

	private Set<IStreamableFileService> services = new HashSet<IStreamableFileService>();

	private static final class SingletonHolder {

		private static final StreamableFileFactory INSTANCE = new StreamableFileFactory();
	}

	public static StreamableFileFactory getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
	public StreamableFileFactory() {
		
		MP3Service mp3FileService = new MP3Service();
		services.add(mp3FileService);
		
		FLVService flvFileService = new FLVService();
		flvFileService.setGenerateMetadata(true);
		services.add(flvFileService);
		
		MP4Service mp4FileService = new MP4Service();
		services.add(mp4FileService);
		
		M4AService m4aFileService = new M4AService();
		services.add(m4aFileService);
	}
	
	/**
	 * Setter for services
	 * 
	 * @param services Set of streamable file services
	 */
	public void setServices(Set<IStreamableFileService> services) {
		logger.debug("StreamableFileFactory set services");
		this.services = services;
	}

	/** {@inheritDoc} */
	public IStreamableFileService getService(File fp) {
		logger.debug("Get service for file: " + fp.getName());
		// Return first service that can handle the passed file
		for (IStreamableFileService service : this.services) {
			if (service.canHandle(fp)) {
				logger.debug("Found service");
				return service;
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	public Set<IStreamableFileService> getServices() {
		logger.debug("StreamableFileFactory get services");
		return services;
	}
}
