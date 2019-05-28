package com.sms.server.api.persistence;

import java.io.IOException;

import com.sms.io.object.Input;
import com.sms.io.object.Output;


public interface IPersistable {

	/**
	 * Prefix for attribute names that should not be made persistent.
	 */
	public static final String TRANSIENT_PREFIX = "_transient";

	/**
	 * Returns <code>true</code> if the object is persistent,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if object is persistent, <code>false</code> otherwise
	 */
	public boolean isPersistent();

	/**
	 * Set the persistent flag of the object.
	 * 
	 * @param persistent <code>true</code> if object is persistent, <code>false</code> otherwise
	 */
	public void setPersistent(boolean persistent);

	/**
	 * Returns the name of the persistent object.
	 * 
	 * @return Object name
	 */
	public String getName();

	/**
	 * Set the name of the persistent object.
	 * 
	 * @param name New object name
	 */
	public void setName(String name);

	/**
	 * Returns the type of the persistent object.
	 * 
	 * @return Object type
	 */
	public String getType();

	/**
	 * Returns the path of the persistent object.
	 * 
	 * @return Persisted object path
	 */
	public String getPath();

	/**
	 * Set the path of the persistent object.
	 * 
	 * @param path New persisted object path
	 */
	public void setPath(String path);

	/**
	 * Returns the timestamp when the object was last modified.
	 * 
	 * @return      Last modification date in milliseconds
	 */
	public long getLastModified();

	/**
	 * Returns the persistence store this object is stored in
	 * 
	 * @return      This object's persistence store
	 */
	public IPersistenceStore getStore();

	/**
	 * Store a reference to the persistence store in the object.
	 * 
	 * @param store
	 * 		Store the object is saved in
	 */
	void setStore(IPersistenceStore store);

	/**
	 * Write the object to the passed output stream.
	 * 
	 * @param output
	 * 		Output stream to write to
     * @throws java.io.IOException     Any I/O exception
	 */
	void serialize(Output output) throws IOException;

	/**
	 * Load the object from the passed input stream.
	 * 
	 * @param input
	 * 		Input stream to load from
     * @throws java.io.IOException      Any I/O exception
	 */
	void deserialize(Input input) throws IOException;
}
