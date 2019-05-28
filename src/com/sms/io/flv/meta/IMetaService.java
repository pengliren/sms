package com.sms.io.flv.meta;

import java.io.File;
import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * IMetaService Defines the MetaData Service API
 */
public interface IMetaService {

	// Get FLV from FLVService
	// grab a reader from FLV	
	// Set up CuePoints
	// Set up MetaData
	// Pass CuePoint array into MetaData
	// read in current MetaData if there is MetaData
	// if there isn't MetaData, write new MetaData
	// Call writeMetaData method on MetaService
	// that in turn will write the current metadata
	// and the cuepoint data
	// after that, call writeMetaCue()
	// this will loop through all the tags making
	// sure that the cuepoints are inserted

	/**
	 * Initiates writing of the MetaData
	 * 
	 * @param meta              Metadata
	 * @throws IOException      I/O exception
	 */
	public void write(IMetaData<?, ?> meta) throws IOException;

	/**
	 * Writes the MetaData
	 * 
	 * @param metaData          Metadata
	 */
	public void writeMetaData(IMetaData<?, ?> metaData);

	/**
	 * Writes the Meta Cue Points
	 */
	public void writeMetaCue();

	/**
	 * Read the MetaData
	 * 
	 * @return metaData         Metadata
     * @param buffer            IoBuffer source
	 */
	public MetaData<?, ?> readMetaData(IoBuffer buffer);

	/**
	 * Read the Meta Cue Points
	 * 
	 * @return  Meta cue points
	 */
	public IMetaCue[] readMetaCue();

	/**
	 * Media file to be accessed
	 * 
	 * @param file
	 */
	public void setFile(File file);

	/**
	 * Returns the file being accessed
	 * 
	 * @return
	 */
	public File getFile();

}
