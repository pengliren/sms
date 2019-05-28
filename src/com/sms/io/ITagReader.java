package com.sms.io;

public interface ITagReader {

	/**
	 * Return the file that is loaded.
	 * 
	 * @return the file to be loaded
	 */
	public IStreamableFile getFile();

	/**
	 * Returns the offet length
	 * 
	 * @return int
	 */
	public int getOffset();

	/**
	 * Returns the amount of bytes read
	 * 
	 * @return long
	 */
	public long getBytesRead();

	/**
	 * Return length in seconds
     * @return length in seconds
     */
	public long getDuration();

	/**
	 * Get the total readable bytes in a file or ByteBuffer
	 *
	 * @return          Total readable bytes
	 */
	public long getTotalBytes();
	
	/**
	 * Decode the header of the stream;
	 *
	 */
	public void decodeHeader();

	/**
	 * Move the reader pointer to given position in file.
	 * 
	 * @param pos File position to move to
	 */
	public void position(long pos);

	/**
	 * Returns a boolean stating whether the FLV has more tags
	 * 
	 * @return boolean
	 */
	public boolean hasMoreTags();

	/**
	 * Returns a Tag object
	 * 
	 * @return Tag
	 */
	public ITag readTag();

	/**
	 * Closes the reader and free any allocated memory.
	 */
	public void close();
	
	/**
	 * Check if the reader also has video tags.
	 * 
	 * @return has video
	 */
	public boolean hasVideo();
	
	public ITagReader copy();

}
