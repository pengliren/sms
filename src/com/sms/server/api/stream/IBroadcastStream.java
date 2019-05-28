package com.sms.server.api.stream;

import java.io.IOException;
import java.util.Collection;

import com.sms.server.messaging.IProvider;
import com.sms.server.net.rtmp.event.Notify;

/**
 * A broadcast stream is a stream source to be subscribed by clients. To
 * subscribe a stream from your client Flash application use NetStream.play
 * method. Broadcast stream can be saved at server-side.
 */
public interface IBroadcastStream extends IStream {

	/**
	 * Save the broadcast stream as a file. 
	 * 
	 * @param filePath
	 *            The path of the file relative to the scope.
	 * @param isAppend
	 *            Whether to append to the end of file.
	 * @throws IOException
	 * 			   File could not be created/written to.
	 * @throws ResourceExistException
	 *             Resource exist when trying to create.
	 * @throws ResourceNotFoundException
	 *             Resource not exist when trying to append.
	 */
	void saveAs(String filePath, boolean isAppend)
            throws IOException, ResourceNotFoundException, ResourceExistException;

	/**
	 * Get the filename the stream is being saved as.
	 * 
	 * @return	The filename relative to the scope or <code>null</code>
	 * 			if the stream is not being saved. 
	 */
	String getSaveFilename();
	
	/**
	 * Get the provider corresponding to this stream. Provider objects are
	 * object that
	 * 
	 * @return the provider
	 */
	IProvider getProvider();

	/**
	 * Get stream publish name. Publish name is the value of the first parameter
	 * had been passed to <code>NetStream.publish</code> on client side in
	 * SWF.
	 * 
	 * @return	Stream publish name	
	 */
	String getPublishedName();

	/**
	 * 
	 * @param name
	 *            Set stream publish name
	 */
	void setPublishedName(String name);

	/**
	 * Add a listener to be notified about received packets.
	 * 
	 * @param listener the listener to add
	 */
	public void addStreamListener(IStreamListener listener);
	
	/**
	 * Remove a listener from being notified about received packets.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeStreamListener(IStreamListener listener);
	
	/**
	 * Return registered stream listeners.
	 * 
	 * @return the registered listeners
	 */
	public Collection<IStreamListener> getStreamListeners();
	
	/**
	 * Returns the metadata for the associated stream, if it exists.
	 * 
	 * @return stream meta data
	 */
	public Notify getMetaData();
	
}