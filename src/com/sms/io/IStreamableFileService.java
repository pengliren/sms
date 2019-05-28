package com.sms.io;

import java.io.File;
import java.io.IOException;

/**
 * Provides access to files that can be streamed. 
 */
public interface IStreamableFileService {

	/**
	 * Sets the prefix.
	 * 
	 * @param prefix
	 */
	public void setPrefix(String prefix);
	
	/**
     * Getter for prefix. Prefix is used in filename composition to fetch real file name.
     *
     * @return  Prefix
     */
    public String getPrefix();

    /**
     * Sets the file extensions serviced. If there are more than one, they are separated
     * by commas.
     * 
     * @param extension
     */
    public void setExtension(String extension);
    
	/**
     * Getter for extension of file
     *
     * @return  File extension that is used
     */
    public String getExtension();

    /**
     * Prepair given string to conform filename requirements, for example, add
     * extension to the end if missing.
     * @param name            String to format
     * @return                Correct filename
     */
    public String prepareFilename(String name);

    /**
     * Check whether file can be used by file service, that is, it does exist and have valid extension
     * @param file            File object
     * @return                <code>true</code> if file exist and has valid extension,
     *                        <code>false</code> otherwise
     */
    public boolean canHandle(File file);

    /**
     * Return streamable file reference. For FLV files returned streamable file already has
     * generated metadata injected.
     *
     * @param file             File resource
     * @return                 Streamable file resource
     * @throws IOException     Thrown if there were problems accessing given file
     */
    public IStreamableFile getStreamableFile(File file) throws IOException;

}
