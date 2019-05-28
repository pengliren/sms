package com.sms.server.api.statistics;

/**
 * Statistics informations about a shared object.
 *
 */
public interface ISharedObjectStatistics extends IStatisticsBase {

	/**
	 * Return the name of the shared object.
	 * 
	 * @return the name of the shared object
	 */
	public String getName();
	
	/**
	 * Check if the shared object is persistent.
	 * 
	 * @return <code>True</code> if the shared object is persistent, otherwise <code>False</code>
	 */
	public boolean isPersistent();
	
	/**
	 * Return the version number of the shared object.
	 * 
	 * @return the version
	 */
	public int getVersion();
	
	/**
	 * Return total number of subscribed listeners.
	 * 
	 * @return number of listeners
	 */
	public int getTotalListeners();
	
	/**
	 * Return maximum number of concurrent subscribed listenes.
	 * 
	 * @return number of listeners
	 */
	public int getMaxListeners();
	
	/**
	 * Return current number of subscribed listeners.
	 * 
	 * @return number of listeners
	 */
	public int getActiveListeners();
	
	/**
	 * Return number of attribute changes.
	 * 
	 * @return number of changes
	 */
	public int getTotalChanges();
	
	/**
	 * Return number of attribute deletes.
	 * 
	 * @return number of deletes
	 */
	public int getTotalDeletes();
	
	/**
	 * Return number of times a message was sent.
	 * 
	 * @return number of sends
	 */
	public int getTotalSends();
}
