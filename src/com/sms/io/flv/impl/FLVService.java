package com.sms.io.flv.impl;

import java.io.File;
import java.io.IOException;

import com.sms.io.BaseStreamableFileService;
import com.sms.io.IStreamableFile;
import com.sms.io.flv.IFLVService;

/**
 * A FLVServiceImpl sets up the service and hands out FLV objects to 
 * its callers.
 */
public class FLVService extends BaseStreamableFileService implements IFLVService {

	/**
	 * Generate FLV metadata?
	 */
	private boolean generateMetadata;

	/** {@inheritDoc} */
	@Override
	public String getPrefix() {
		return "flv";
	}

	/** {@inheritDoc} */
	@Override
	public String getExtension() {
		return ".flv";
	}

	/** {@inheritDoc}
	 */
	@Override
	public IStreamableFile getStreamableFile(File file) throws IOException {
		return new FLV(file, generateMetadata);
	}

	/**
	 * Generate metadata or not
	 *
	 * @param generate  <code>true</code> if there's need to generate metadata, <code>false</code> otherwise
	 */
	public void setGenerateMetadata(boolean generate) {
		generateMetadata = generate;
	}
}
