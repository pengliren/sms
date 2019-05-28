package com.sms.server;

import com.sms.server.api.IGlobalScope;
import com.sms.server.api.IServer;
import com.sms.server.api.persistence.IPersistenceStore;
import com.sms.server.api.persistence.PersistenceUtils;

public class GlobalScope extends Scope implements IGlobalScope {

	// Red5 Server instance
	protected IServer server;

	/**
	 * 
	 * @param persistenceClass Persistent class name
	 * @throws Exception Exception
	 */
	@Override
	public void setPersistenceClass(String persistenceClass) throws Exception {
		this.persistenceClass = persistenceClass;
		// We'll have to wait for creation of the store object
		// until all classes have been initialized.
	}

	/**
	 * Get persistence store for scope
	 * 
	 * @return Persistence store
	 */
	@Override
	public IPersistenceStore getStore() {
		if (store != null) {
			return store;
		}

		try {
			store = PersistenceUtils.getPersistenceStore(this, this.persistenceClass);
		} catch (Exception error) {
			log.error("Could not create persistence store.", error);
			store = null;
		}
		return store;
	}

	/**
	 * Setter for server
	 * 
	 * @param server Server
	 */
	public void setServer(IServer server) {
		this.server = server;
	}

	/** {@inheritDoc} */
	@Override
	public IServer getServer() {
		return server;
	}

	/**
	 * Register global scope in server instance, then call initialization
	 */
	public void register() {
		server.registerGlobal(this);
		init();
	}
}
