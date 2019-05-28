package com.sms.server;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sms.server.api.IClient;
import com.sms.server.api.IClientRegistry;
import com.sms.server.exception.ClientNotFoundException;
import com.sms.server.exception.ClientRejectedException;

public class ClientRegistry implements IClientRegistry {

	/**
	 * Clients map
	 */
	private ConcurrentMap<String, IClient> clients = new ConcurrentHashMap<String, IClient>();

	/**
	 *  Next client id
	 */
	private AtomicInteger nextId = new AtomicInteger();

	private static final class SingletonHolder {

		private static final ClientRegistry INSTANCE = new ClientRegistry();
	}

	public static ClientRegistry getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
	public ClientRegistry() {
		
	}

	/**
	 * Add client to registry
	 * @param client           Client to add
	 */
	protected void addClient(IClient client) {
		addClient(client.getId(), client);
	}

	/**
	 * Add the client to the registry
	 */
	private void addClient(String id, IClient client) {
		//check to see if the id already exists first
		if (!hasClient(id)) {
			clients.put(id, client);
		} else {
			//get the next available client id
			String newId = nextId();
			//update the client
			client.setId(newId);
			//add the client to the list
			addClient(newId, client);
		}
	}

	public Client getClient(String id) throws ClientNotFoundException {
		Client result = (Client) clients.get(id);
		if (result == null) {
			throw new ClientNotFoundException(id);
		}
		return result;
	}

	/**
	 * Returns a list of Clients.
	 */
	public ClientList<Client> getClientList() {
		ClientList<Client> list = new ClientList<Client>();
		for (IClient c : clients.values()) {
			list.add((Client) c);
		}
		return list;
	}

	/**
	 * Check if client registry contains clients.
	 * 
	 * @return             <code>True</code> if clients exist, otherwise <code>False</code>
	 */
	protected boolean hasClients() {
		return !clients.isEmpty();
	}

	/**
	 * Return collection of clients
	 * @return             Collection of clients
	 */
	@SuppressWarnings("unchecked")
	protected Collection<IClient> getClients() {
		if (!hasClients()) {
			// Avoid creating new Collection object if no clients exist.
			return Collections.EMPTY_SET;
		}
		return Collections.unmodifiableCollection(clients.values());
	}

	/**
	 * Check whether registry has client with given id
	 *
	 * @param id         Client id
	 * @return           true if client with given id was register with this registry, false otherwise
	 */
	public boolean hasClient(String id) {
		if (id == null) {
			// null ids are not supported
			return false;
		}
		return clients.containsKey(id);
	}

	/**
	 * Return client by id
	 *
	 * @param id          Client id
	 * @return            Client object associated with given id
	 * @throws ClientNotFoundException if we can't find client
	 */
	public IClient lookupClient(String id) throws ClientNotFoundException {
		return getClient(id);
	}

	/**
	 * Return client from next id with given params
	 *
	 * @param params                         Client params
	 * @return                               Client object
	 * @throws ClientNotFoundException if client not found
	 * @throws ClientRejectedException if client rejected
	 */
	public IClient newClient(Object[] params) throws ClientNotFoundException, ClientRejectedException {
		String id = nextId();
		IClient client = new Client(id, this);
		addClient(id, client);
		return client;
	}

	/**
	 * Return next client id
	 * @return         Next client id
	 */
	public String nextId() {
		//when we reach max int, reset to zero
		if (nextId.get() == Integer.MAX_VALUE) {
			nextId.set(0);
		}
		return String.format("%s", nextId.getAndIncrement());
	}

	/**
	 * Return previous client id
	 * @return        Previous client id
	 */
	public String previousId() {
		return String.format("%s", nextId.get());
	}

	/**
	 * Removes client from registry
	 * @param client           Client to remove
	 */
	protected void removeClient(IClient client) {
		clients.remove(client.getId());
	}
}
