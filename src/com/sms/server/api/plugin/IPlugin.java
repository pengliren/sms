package com.sms.server.api.plugin;

import com.sms.server.Server;

public interface IPlugin {

	/**
	 * Returns a name / identifier for the plug-in.
	 * 
	 * @return plug-in's name
	 */
	String getName();
	
	/**
	 * Sets the top-most ApplicationContext within Red5.
	 * 
	 * @param context
	 */
	//void setApplicationContext(ApplicationContext context);	
	
	/**
	 * Sets a reference to the server.
	 * 
	 * @param server
	 */
	void setServer(Server server);

	/**
	 * Lifecycle method called when the plug-in is started.
	 * 
	 * @throws Exception 
	 */
	void doStart() throws Exception;
		
	/**
	 * Lifecycle method called when the plug-in is stopped.
	 * 
	 * @throws Exception 
	 */
	void doStop() throws Exception;
}
