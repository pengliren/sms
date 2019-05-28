package com.sms.server.api.plugin;

import java.util.Map;

import com.sms.server.adapter.MultiThreadedApplicationAdapter;

public interface IPluginHandler {

	/**
	 * Initialize the plug-in handler.
	 */
	void init();
	
	/**
	 * Set the application making use of this plug-in handler.
	 * 
	 * @param application
	 */
	void setApplication(MultiThreadedApplicationAdapter application);

	/**
	 * Set properties to be used by this handler.
	 * 
	 * @param props
	 */
	void setProperties(Map<String, Object> props);
}
