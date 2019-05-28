package com.sms.io.flv.impl;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.ITag;
import com.sms.io.ITagReader;
import com.sms.io.ITagWriter;
import com.sms.io.IoConstants;
import com.sms.io.flv.IFLV;
import com.sms.io.flv.meta.IMetaData;
import com.sms.io.flv.meta.IMetaService;
import com.sms.io.flv.meta.MetaData;
import com.sms.io.flv.meta.MetaService;

/**
 * A FLVImpl implements the FLV api
 */
public class FLV implements IFLV {

	protected static Logger log = LoggerFactory.getLogger(FLV.class);

	private File file;

	private boolean generateMetadata;

	private IMetaService metaService;

	private IMetaData<?, ?> metaData;

	/**
	 * Default constructor, used by Spring so that parameters may be injected.
	 */
	public FLV() {
	}

	/**
	 * Create FLV from given file source
	 * 
	 * @param file File source
	 */
	public FLV(File file) {
		this(file, false);
	}

	/**
	 * Create FLV from given file source and with specified metadata generation
	 * option
	 * 
	 * @param file File source
	 * @param generateMetadata Metadata generation option
	 */
	public FLV(File file, boolean generateMetadata) {
		this.file = file;
		this.generateMetadata = generateMetadata;
		int count = 0;
		if (!generateMetadata) {
			try {
				FLVReader reader = new FLVReader(this.file);
				ITag tag = null;
				while (reader.hasMoreTags() && (++count < 5)) {
					tag = reader.readTag();
					if (tag.getDataType() == IoConstants.TYPE_METADATA) {
						if (metaService == null) {
							metaService = new MetaService(this.file);
						}
						metaData = metaService.readMetaData(tag.getBody());
					}
				}
				reader.close();
			} catch (Exception e) {
				log.error("An error occured looking for metadata", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasMetaData() {
		return metaData != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes" })
	public IMetaData getMetaData() throws FileNotFoundException {
		metaService.setFile(file);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasKeyFrameData() {
//		if (hasMetaData()) {
//			return !((MetaData) metaData).getKeyframes().isEmpty();
//		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setKeyFrameData(Map keyframedata) {
		if (!hasMetaData()) {
			metaData = new MetaData();
		}
		//The map is expected to contain two entries named "times" and "filepositions",
		//both of which contain a map keyed by index and time or position values.
		Map<String, Double> times = new HashMap<String, Double>();
		Map<String, Double> filepositions = new HashMap<String, Double>();
		//
		if (keyframedata.containsKey("times")) {
			Map inTimes = (Map) keyframedata.get("times");
			for (Object o : inTimes.entrySet()) {
				Map.Entry<String, Double> entry = (Map.Entry<String, Double>) o;
				times.put(entry.getKey(), entry.getValue());
			}
		}
		((MetaData) metaData).put("times", times);
		//
		if (keyframedata.containsKey("filepositions")) {
			Map inPos = (Map) keyframedata.get("filepositions");
			for (Object o : inPos.entrySet()) {
				Map.Entry<String, Double> entry = (Map.Entry<String, Double>) o;
				filepositions.put(entry.getKey(), entry.getValue());
			}			
		}
		((MetaData) metaData).put("filepositions", filepositions);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes" })
	public Map getKeyFrameData() {
		Map keyframes = null;
//		if (hasMetaData()) {
//			keyframes = ((MetaData) metaData).getKeyframes();
//		}
		return keyframes;
	}

	/**
	 * {@inheritDoc}
	 */
	public void refreshHeaders() throws IOException {
	}

	/**
	 * {@inheritDoc}
	 */
	public void flushHeaders() throws IOException {
	}

	/**
	 * {@inheritDoc}
	 */
	public ITagReader getReader() throws IOException {
		FLVReader reader = null;
		if (file.exists()) {
			log.debug("File size: {}", file.length());
			reader = new FLVReader(file, generateMetadata);

		} else {
			log.info("Creating new file: {}", file);
			file.createNewFile();
		}
		return reader;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITagReader readerFromNearestKeyFrame(int seekPoint) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITagWriter getWriter() throws IOException {
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		ITagWriter writer = new FLVWriter(file, false);
		return writer;
	}

	/** {@inheritDoc} */
	public ITagWriter getAppendWriter() throws IOException {
		// If the file doesn't exist, we can't append to it, so return a writer
		if (!file.exists()) {
			log.info("File does not exist, calling writer. This will create a new file.");
			return getWriter();
		}
		//Fix by Mhodgson: FLVWriter constructor allows for passing of file object
		ITagWriter writer = new FLVWriter(file, true);
		return writer;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITagWriter writerFromNearestKeyFrame(int seekPoint) {
		return null;
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "rawtypes" })
	public void setMetaData(IMetaData meta) throws IOException {
		if (metaService == null) {
			metaService = new MetaService(file);
		}
		//if the file is not checked the write may produce an NPE
		if (metaService.getFile() == null) {
			metaService.setFile(file);
		}
		metaService.write(meta);
		metaData = meta;
	}

	/** {@inheritDoc} */
	public void setMetaService(IMetaService service) {
		metaService = service;
	}
}
