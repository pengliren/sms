package com.sms.server.plugin;

import com.sms.server.Server;
import com.sms.server.adapter.MultiThreadedApplicationAdapter;
import com.sms.server.api.plugin.IPlugin;

public abstract class Plugin implements IPlugin {

	protected Server server;

	/** {@inheritDoc} */
	public void doStart() throws Exception {
	}

	/** {@inheritDoc} */
	public void doStop() throws Exception {
	}

	/**
	 * Initialize the plug-in
	 */
	public void init() {
	}

	/** {@inheritDoc} */
	public String getName() {
		return null;
	}

	/**
	 * Return the server reference.
	 * 
	 * @return server
	 */
	public Server getServer() {
		return server;
	}

	/** {@inheritDoc} */
	public void setServer(Server server) {
		this.server = server;
	}

	/**
	 * Set the application making use of this plug-in.
	 * 
	 * @param application
	 */
	public void setApplication(MultiThreadedApplicationAdapter application) {
	}
}
