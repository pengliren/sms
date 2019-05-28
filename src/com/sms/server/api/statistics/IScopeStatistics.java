package com.sms.server.api.statistics;


public interface IScopeStatistics extends IStatisticsBase {

	/**
	 * Get the name of this scope. Eg. <code>someroom</code>.
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Get the full absolute path. Eg. <code>host/myapp/someroom</code>.
	 * 
	 * @return Absolute scope path
	 */
	public String getPath();

	/**
	 * Get the scopes depth, how far down the scope tree is it. The lowest depth
	 * is 0x00, the depth of Global scope. Application scope depth is 0x01. Room
	 * depth is 0x02, 0x03 and so forth.
	 * 
	 * @return the depth
	 */
	public int getDepth();
	
	/**
	 * Return total number of connections to the scope.
	 * 
	 * @return number of connections
	 */
	public int getTotalConnections();
	
	/**
	 * Return maximum number of concurrent connections to the scope.
	 * 
	 * @return number of connections
	 */
	public int getMaxConnections();
	
	/**
	 * Return current number of connections to the scope.
	 * 
	 * @return number of connections
	 */
	public int getActiveConnections();
	
	/**
	 * Return total number of clients connected to the scope.
	 * 
	 * @return number of clients
	 */
	public int getTotalClients();
	
	/**
	 * Return maximum number of clients concurrently connected to the scope.
	 * 
	 * @return number of clients
	 */
	public int getMaxClients();
	
	/**
	 * Return current number of clients connected to the scope.
	 * 
	 * @return number of clients
	 */
	public int getActiveClients();
	
	/**
	 * Return total number of subscopes created.
	 * 
	 * @return number of subscopes created
	 */
	public int getTotalSubscopes();
	
	/**
	 * Return maximum number of concurrently existing subscopes.
	 * 
	 * @return number of subscopes
	 */
	public int getMaxSubscopes();
	
	/**
	 * Return number of currently existing subscopes.
	 * 
	 * @return number of subscopes
	 */
	public int getActiveSubscopes();
}
