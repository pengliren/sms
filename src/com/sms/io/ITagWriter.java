package com.sms.io;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Writes tags to FLV file
 */
public interface ITagWriter {

	/**
	 * Return the file that is written.
	 * 
	 * @return the File to be written
	 */
	public IStreamableFile getFile();

	/**
	 * Return the offset
	 * 
	 * @return  Offset value
	 */
	public int getOffset();

	/**
	 * Return the bytes written
	 * 
	 * @return  Number of bytes written
	 */
	public long getBytesWritten();

	/**
	 * Writes the header bytes
	 * 
	 * @throws IOException           I/O exception
	 */
	public void writeHeader() throws IOException;

	/**
	 * Writes a Tag object
	 * 
	 * @param tag                    Tag to write
	 * @return                       <code>true</code> on success, <code>false</code> otherwise
	 * @throws IOException           I/O exception
	 */
	public boolean writeTag(ITag tag) throws IOException;

	/**
	 * Write a Tag using bytes
	 * 
	 * @param type                   Tag type
	 * @param data                   Byte data
	 * @return                       <code>true</code> on success, <code>false</code> otherwise
	 * @throws IOException           I/O exception
	 */
	public boolean writeTag(byte type, IoBuffer data) throws IOException;

	/**
	 * Write a Stream to disk using bytes
	 * 
	 * @param b                      Array of bytes to write
	 * @return                       <code>true</code> on success, <code>false</code> otherwise
	 */
	public boolean writeStream(byte[] b);

	/**
	 * Closes a Writer
	 */
	public void close();

}
