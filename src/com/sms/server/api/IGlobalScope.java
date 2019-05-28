package com.sms.server.api;



public interface IGlobalScope extends IScope {

	/**
	 * Register the global scope in the server and initialize it.
	 * 
	 */
	public void register();

	/**
	 * Return the server this global scope runs in.
	 * 
	 * @return the server
	 */
	public IServer getServer();
}
