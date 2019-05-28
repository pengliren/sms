package com.sms.server;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IConnection;
import com.sms.server.api.IGlobalScope;
import com.sms.server.api.IScope;
import com.sms.server.api.IServer;
import com.sms.server.api.listeners.IConnectionListener;
import com.sms.server.api.listeners.IScopeListener;
import com.sms.server.api.scheduling.IScheduledJob;
import com.sms.server.api.scheduling.ISchedulingService;
import com.sms.server.scheduling.QuartzSchedulingService;

public class Server implements IServer {

	protected static Logger log = LoggerFactory.getLogger(Server.class);

	/**
	 * Service used to provide notifications.
	 */
	private static QuartzSchedulingService schedulingService;
	
	/**
	 * List of global scopes
	 */
	protected ConcurrentMap<String, IGlobalScope> globals = new ConcurrentHashMap<String, IGlobalScope>();

	/**
	 * Mappings
	 */
	protected ConcurrentMap<String, String> mapping = new ConcurrentHashMap<String, String>();
	
	/**
	 * Constant for slash
	 */
	protected static final String SLASH = "/";

	/**
	 * Constant for empty string
	 */
	protected static final String EMPTY = "";

	public Set<IScopeListener> scopeListeners = new CopyOnWriteArraySet<IScopeListener>();

	public Set<IConnectionListener> connectionListeners = new CopyOnWriteArraySet<IConnectionListener>();

	private static final class SingletonHolder {

		private static final Server INSTANCE = new Server();
	}

	public static Server getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
	private Server() {
		Server.schedulingService = QuartzSchedulingService.getInstance();
		
		GlobalScope defualt = new GlobalScope();
		defualt.setName("default");		
		defualt.setServer(this);
		
		Context ctx = new Context();
		ScopeResolver scr = new ScopeResolver();
		scr.setGlobalScope(defualt);
		ctx.setScopeResolver(scr);
		
		CoreHandler coreHandle = new CoreHandler();
		defualt.setHandler(coreHandle);
		defualt.setContext(ctx);
		defualt.register();
	}
	
	/**
	 * Return scope key. Scope key consists of host name concatenated with
	 * context path by slash symbol
	 * 
	 * @param hostName Host name
	 * @param contextPath Context path
	 * @return Scope key as string
	 */
	protected String getKey(String hostName, String contextPath) {
		if (hostName == null) {
			hostName = EMPTY;
		}
		if (contextPath == null) {
			contextPath = EMPTY;
		}
		return hostName + SLASH + contextPath;
	}

	/**
	 * Does global scope lookup for host name and context path
	 * 
	 * @param hostName Host name
	 * @param contextPath Context path
	 * @return Global scope
	 */
	public IGlobalScope lookupGlobal(String hostName, String contextPath) {
		log.trace("{}", this);
		log.debug("Lookup global scope - host name: {} context path: {}", hostName, contextPath);
		// Init mappings key
		String key = getKey(hostName, contextPath);
		// If context path contains slashes get complex key and look for it
		// in mappings
		while (contextPath.indexOf(SLASH) != -1) {
			key = getKey(hostName, contextPath);
			log.trace("Check: {}", key);
			String globalName = mapping.get(key);
			if (globalName != null) {
				return getGlobal(globalName);
			}
			final int slashIndex = contextPath.lastIndexOf(SLASH);
			// Context path is substring from the beginning and till last slash
			// index
			contextPath = contextPath.substring(0, slashIndex);
		}

		// Get global scope key
		key = getKey(hostName, contextPath);
		log.trace("Check host and path: {}", key);

		// Look up for global scope switching keys if still not found
		String globalName = mapping.get(key);
		if (globalName != null) {
			return getGlobal(globalName);
		}
		key = getKey(EMPTY, contextPath);
		log.trace("Check wildcard host with path: {}", key);
		globalName = mapping.get(key);
		if (globalName != null) {
			return getGlobal(globalName);
		}
		key = getKey(hostName, EMPTY);
		log.trace("Check host with no path: {}", key);
		globalName = mapping.get(key);
		if (globalName != null) {
			return getGlobal(globalName);
		}
		key = getKey(EMPTY, EMPTY);
		log.trace("Check default host, default path: {}", key);
		return getGlobal(mapping.get(key));
	}

	/**
	 * Return global scope by name
	 * 
	 * @param name Global scope name
	 * @return Global scope
	 */
	public IGlobalScope getGlobal(String name) {
		if (name == null) {
			return null;
		}
		return globals.get(name);
	}

	/**
	 * Register global scope
	 * 
	 * @param scope Global scope to register
	 */
	public void registerGlobal(IGlobalScope scope) {
		log.trace("Registering global scope: {}", scope.getName(), scope);
		globals.put(scope.getName(), scope);
	}

	/**
	 * Map key (host + / + context path) and global scope name
	 * 
	 * @param hostName Host name
	 * @param contextPath Context path
	 * @param globalName Global scope name
	 * @return true if mapping was added, false if already exist
	 */
	public boolean addMapping(String hostName, String contextPath, String globalName) {
		log.info("Add mapping global: {} host: {} context: {}", new Object[] { globalName, hostName, contextPath });
		final String key = getKey(hostName, contextPath);
		log.debug("Add mapping: {} => {}", key, globalName);
		return (mapping.putIfAbsent(key, globalName) == null);
	}

	/**
	 * Remove mapping with given key
	 * 
	 * @param hostName Host name
	 * @param contextPath Context path
	 * @return true if mapping was removed, false if key doesn't exist
	 */
	public boolean removeMapping(String hostName, String contextPath) {
		log.info("Remove mapping host: {} context: {}", hostName, contextPath);
		final String key = getKey(hostName, contextPath);
		log.debug("Remove mapping: {}", key);
		return (mapping.remove(key) != null);
	}

	/**
	 * Remove all mappings with given context path
	 * 
	 * @param contextPath Context path
	 * @return true if mapping was removed, false if key doesn't exist
	 */
	public boolean removeMapping(String contextPath) {
		log.info("Remove mapping context: {}", contextPath);
		final String key = getKey("", contextPath);
		log.debug("Remove mapping: {}", key);
		return (mapping.remove(key) != null);
	}	
	
	/**
	 * Return mapping
	 * 
	 * @return Map of "scope key / scope name" pairs
	 */
	public Map<String, String> getMappingTable() {
		return mapping;
	}

	/**
	 * Return global scope names set iterator
	 * 
	 * @return Iterator
	 */
	public Iterator<String> getGlobalNames() {
		return globals.keySet().iterator();
	}

	/**
	 * Return global scopes set iterator
	 * 
	 * @return Iterator
	 */
	public Iterator<IGlobalScope> getGlobalScopes() {
		return globals.values().iterator();
	}

	/** {@inheritDoc} */
	public void addListener(IScopeListener listener) {
		scopeListeners.add(listener);
	}

	/** {@inheritDoc} */
	public void addListener(IConnectionListener listener) {
		connectionListeners.add(listener);
	}

	/** {@inheritDoc} */
	public void removeListener(IScopeListener listener) {
		scopeListeners.remove(listener);
	}

	/** {@inheritDoc} */
	public void removeListener(IConnectionListener listener) {
		connectionListeners.remove(listener);
	}

	/**
	 * Notify listeners about a newly created scope.
	 * 
	 * @param scope
	 *            the scope that was created
	 */
	protected void notifyScopeCreated(final IScope scope) {
		schedulingService.addScheduledOnceJob(10, new ScopeCreatedJob(scope));
	}

	/**
	 * Notify listeners that a scope was removed.
	 * 
	 * @param scope
	 *            the scope that was removed
	 */
	protected void notifyScopeRemoved(final IScope scope) {
		schedulingService.addScheduledOnceJob(10, new ScopeRemovedJob(scope));	
	}

	/**
	 * Notify listeners that a new connection was established.
	 * 
	 * @param conn
	 *            the new connection
	 */
	protected void notifyConnected(final IConnection conn) {
		schedulingService.addScheduledOnceJob(10, new ConnectedJob(conn));
	}

	/**
	 * Notify listeners that a connection was disconnected.
	 * 
	 * @param conn
	 *            the disconnected connection
	 */
	protected void notifyDisconnected(final IConnection conn) {
		schedulingService.addScheduledOnceJob(10, new DisconnectedJob(conn));	
	}

	/**
	 * Used to indicate a scope was created.
	 */
	private final class ScopeCreatedJob implements IScheduledJob {

		private IScope scope;

		ScopeCreatedJob(IScope scope) {
			this.scope = scope;
		}

		public void execute(ISchedulingService service) {
			for (IScopeListener listener : scopeListeners) {
				listener.notifyScopeCreated(scope);
			}
		}

	}

	/**
	 * Used to indicate a scope was removed.
	 */
	private final class ScopeRemovedJob implements IScheduledJob {

		private IScope scope;

		ScopeRemovedJob(IScope scope) {
			this.scope = scope;
		}

		public void execute(ISchedulingService service) {
			for (IScopeListener listener : scopeListeners) {
				listener.notifyScopeRemoved(scope);
			}
		}
	}

	private final class ConnectedJob implements IScheduledJob {

		private IConnection conn;

		ConnectedJob(IConnection conn) {
			this.conn = conn;
		}

		public void execute(ISchedulingService service) {
			for (IConnectionListener listener : connectionListeners) {
				listener.notifyConnected(conn);
			}
		}
	}
	
	private final class DisconnectedJob implements IScheduledJob {

		private IConnection conn;

		DisconnectedJob(IConnection conn) {
			this.conn = conn;
		}

		public void execute(ISchedulingService service) {
			for (IConnectionListener listener : connectionListeners) {
				listener.notifyDisconnected(conn);
			}
		}
	}	
}
