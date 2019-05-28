package com.sms.io.flv;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import com.sms.io.IStreamableFile;
import com.sms.io.ITagReader;
import com.sms.io.ITagWriter;
import com.sms.io.flv.meta.IMetaData;
import com.sms.io.flv.meta.IMetaService;

/**
 * Represents FLV file
 */
public interface IFLV extends IStreamableFile {

	/**
	 * Returns a boolean stating whether the flv has metadata
	 * 
	 * @return boolean        <code>true</code> if file has injected metadata, <code>false</code> otherwise
	 */
	public boolean hasMetaData();

	/**
	 * Sets the metadata
	 * 
	 * @param metadata                   Metadata object
	 * @throws FileNotFoundException     File not found
	 * @throws IOException               Any other I/O exception
	 */
	@SuppressWarnings({ "rawtypes" })
	public void setMetaData(IMetaData metadata) throws FileNotFoundException,
			IOException;

	/**
	 * Sets the MetaService through Spring
	 * 
	 * @param service                    Metadata service
	 */
	public void setMetaService(IMetaService service);

	/**
	 * Returns a map of the metadata
	 * 
	 * @return metadata                  File metadata
	 * @throws FileNotFoundException     File not found
	 */
	@SuppressWarnings({ "rawtypes" })
	public IMetaData getMetaData() throws FileNotFoundException;

	/**
	 * Returns a boolean stating whether a flv has keyframedata
	 * 
	 * @return boolean                   <code>true</code> if file has keyframe metadata, <code>false</code> otherwise
	 */
	public boolean hasKeyFrameData();

	/**
	 * Sets the keyframe data of a flv file
	 * 
	 * @param keyframedata              Keyframe metadata
	 */
	@SuppressWarnings({ "rawtypes" })
	public void setKeyFrameData(Map keyframedata);

	/**
	 * Gets the keyframe data
	 * 
	 * @return keyframedata             Keyframe metadata
	 */
	@SuppressWarnings({ "rawtypes" })
	public Map getKeyFrameData();

	/**
	 * Refreshes the headers. Usually used after data is added to the flv file
	 * 
	 * @throws IOException              Any I/O exception
	 */
	public void refreshHeaders() throws IOException;

	/**
	 * Flushes Header
	 * 
	 * @throws IOException              Any I/O exception
	 */
	public void flushHeaders() throws IOException;

	/**
	 * Returns a Reader closest to the nearest keyframe
	 * 
	 * @param seekPoint                Point in file we are seeking around
	 * @return reader                  Tag reader closest to that point
	 */
	public ITagReader readerFromNearestKeyFrame(int seekPoint);

	/**
	 * Returns a Writer based on the nearest key frame
	 * 
	 * @param seekPoint                Point in file we are seeking around
	 * @return writer                  Tag writer closest to that point
	 */
	public ITagWriter writerFromNearestKeyFrame(int seekPoint);
}
