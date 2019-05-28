package com.sms.server.api;

import com.sms.server.exception.ClientNotFoundException;
import com.sms.server.exception.ClientRejectedException;


public interface IClientRegistry {

	/**
	 * Check if a client with a given id exists.
	 * 
	 * @param id the id of the client to check for
	 * @return <code>true</code> if the client exists, <code>false</code> otherwise
	 */
	public boolean hasClient(String id);

	/**
	 * Create a new client client object from connection params.
	 * 
	 * @param params the parameters the client passed during connection
	 * @return the new client
	 * @throws ClientNotFoundException no client could be created from the passed parameters
	 * @throws ClientRejectedException the client is not allowed to connect
	 */
	public IClient newClient(Object[] params) throws ClientNotFoundException,
			ClientRejectedException;

	/**
	 * Return an existing client from a client id.
	 *  
	 * @param id the id of the client to return
	 * @return the client object
	 * @throws ClientNotFoundException no client with the passed id exists
	 */
	public IClient lookupClient(String id) throws ClientNotFoundException;
}
