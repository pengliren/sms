package com.sms.server;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IConnection;
import com.sms.server.api.IGlobalScope;
import com.sms.server.api.IServer;

public class WebScope extends Scope {

	/**
	 * Logger
	 */
	protected static Logger log = LoggerFactory.getLogger(WebScope.class);

	/**
	 * Server instance
	 */
	protected IServer server;

	/**
	 * Context path
	 */
	protected String contextPath;

	/**
	 * Virtual hosts list as string
	 */
	protected String virtualHosts;

	/**
	 * Hostnames
	 */
	protected String[] hostnames;

	/**
	 * Has the web scope been registered?
	 */
	protected AtomicBoolean registered = new AtomicBoolean(false);

	/**
	 * The application context this webscope is running in.
	 */
	//protected IApplicationContext appContext;

	/**
	 * Loader for new applications.
	 */
	//protected IApplicationLoader appLoader;

	/**
	 * Is the scope currently shutting down?
	 */
	protected AtomicBoolean shuttingDown = new AtomicBoolean(false);

	/**
	 * Setter for global scope. Sets persistence class.
	 * 
	 * @param globalScope Red5 global scope
	 */
	public void setGlobalScope(IGlobalScope globalScope) {
		log.trace("Set global scope: {}", globalScope);
		// XXX: this is called from nowhere, remove?
		super.setParent(globalScope);
		try {
			setPersistenceClass(globalScope.getStore().getClass().getName());
		} catch (Exception error) {
			log.error("Could not set persistence class.", error);
		}
	}

	/**
	 * Web scope has no name
	 */
	public void setName() {
		throw new RuntimeException("Cannot set name, you must set context path");
	}

	/**
	 * Can't set parent to Web scope. Web scope is top level.
	 */
	public void setParent() {
		throw new RuntimeException("Cannot set parent, you must set global scope");
	}

	/**
	 * Setter for server
	 * 
	 * @param server Server instance
	 */
	public void setServer(IServer server) {
		log.info("Set server {}", server);
		this.server = server;
	}

	/**
	 * Servlet context
	 * 
	 * @param servletContext Servlet context
	 
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}*/

	/**
	 * Setter for context path
	 * 
	 * @param contextPath Context path
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
		super.setName(contextPath.substring(1));
	}

	/**
	 * Setter for virtual hosts. Creates array of hostnames.
	 * 
	 * @param virtualHosts Virtual hosts list as string
	 */
	public void setVirtualHosts(String virtualHosts) {
		this.virtualHosts = virtualHosts;
		// Split string into array of vhosts
		hostnames = virtualHosts.split(",");
		for (int i = 0; i < hostnames.length; i++) {
			hostnames[i] = hostnames[i].trim();
			if (hostnames[i].equals("*")) {
				hostnames[i] = "";
			}
		}
	}

	/**
	 * Map all vhosts to global scope then initialize
	 */
	public void register() {
		if (registered.get()) {
			log.info("Webscope already registered");
			return;
		}
		log.debug("Webscope registering: {}", contextPath);		
		//getAppContext();
		//appLoader = LoaderBase.getApplicationLoader();
		//get the parent name
		String parentName = getParent().getName();
		//add host name mappings
		if (hostnames != null && hostnames.length > 0) {
			for (String hostName : hostnames) {
				server.addMapping(hostName, getName(), parentName);
			}
		}
		init();
		// We don't want to have configured scopes to get freed when a client
		// disconnects.
		keepOnDisconnect = true;
		registered.set(true);
	}

	/**
	 * Uninitialize and remove all vhosts from the global scope.
	 */
	public void unregister() {
		if (!registered.get()) {
			log.info("Webscope not registered");
			return;
		}
		log.debug("Webscope un-registering: {}", contextPath);	
		shuttingDown.set(true);
		keepOnDisconnect = false;
		uninit();
		// We need to disconnect all clients before unregistering
		Collection<Set<IConnection>> conns = getConnections();
		for (Set<IConnection> set : conns) {
			for (IConnection conn : set) {
				conn.close();
			}
			//should we clear the set?
			set.clear();
		}
		//
		conns.clear();
		//
		if (hostnames != null && hostnames.length > 0) {
			for (String element : hostnames) {
				server.removeMapping(element, getName());
			}
		}
		//check for null
		/*if (appContext == null) {
			log.debug("Application context is null, trying retrieve from loader");
			getAppContext();		
		}
		//try to stop the app context
		if (appContext != null) {
			log.debug("Stopping app context");
			appContext.stop();
		} else {
			log.debug("Application context is null, could not be stopped");
		}*/
		// Various cleanup tasks
		setStore(null);
		//setServletContext(null);
		setServer(null);
		setName(null);
		//appContext = null;
		registered.set(false);
		shuttingDown.set(false);
	}

	/** {@inheritDoc} */
	@Override
	public IServer getServer() {
		return server;
	}

	/**
	 * Return object that can be used to load new applications.
	 * 
	 * @return the application loader
	 
	public IApplicationLoader getApplicationLoader() {
		return appLoader;
	}*/

	/**
	 * Sets the local app context variable based on host id if available in the 
	 * servlet context.
	
	private final void getAppContext() {
		//get the host id
		String hostId = null;
		//get host from servlet context
		if (servletContext != null) {
			ServletContext sctx = servletContext.getContext(contextPath);
			if (sctx != null) {
				hostId = (String) sctx.getAttribute("red5.host.id");
				log.trace("Host id from init param: {}", hostId);
			}
		}		
		if (hostId != null) {
			appContext = LoaderBase.getRed5ApplicationContext(hostId + contextPath);
		} else {
			appContext = LoaderBase.getRed5ApplicationContext(contextPath);
		}
	}	 */
	
	/**
	 * Is the scope currently shutting down?
	 * 
	 * @return is shutting down
	 */
	public boolean isShuttingDown() {
		return shuttingDown.get();
	}
}
