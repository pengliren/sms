package com.sms.server.so;

import static com.sms.server.api.so.ISharedObject.TYPE;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IScope;
import com.sms.server.api.persistence.IPersistable;
import com.sms.server.api.persistence.IPersistenceStore;
import com.sms.server.api.persistence.PersistenceUtils;
import com.sms.server.api.scheduling.IScheduledJob;
import com.sms.server.api.so.ISharedObject;
import com.sms.server.api.so.ISharedObjectService;
import com.sms.server.persistence.RamPersistence;
import com.sms.server.scheduling.QuartzSchedulingService;

/**
 * Shared object service
 */
public class SharedObjectService implements ISharedObjectService {

	/**
	 * Logger
	 */
	private Logger log = LoggerFactory.getLogger(SharedObjectService.class);

	/**
	 * Persistence store prefix
	 */
	private static final String SO_PERSISTENCE_STORE = IPersistable.TRANSIENT_PREFIX + "_SO_PERSISTENCE_STORE_";

	/**
	 * Transient store prefix
	 */
	private static final String SO_TRANSIENT_STORE = IPersistable.TRANSIENT_PREFIX + "_SO_TRANSIENT_STORE_";
	
	/**
	 * Service used to provide updates / notifications.
	 */
	private static QuartzSchedulingService schedulingService;


	/** 
	 * Maximum messages to send at once
	 */
	public static int MAXIMUM_EVENTS_PER_UPDATE = 16;

	/**
	 * Persistence class name
	 */
	private String persistenceClassName = "com.sms.server.persistence.RamPersistence";
	
	private static final class SingletonHolder {

		private static final SharedObjectService INSTANCE = new SharedObjectService();
	}
	
	protected SharedObjectService() {
		
		SharedObjectService.schedulingService = QuartzSchedulingService.getInstance();
	}

	public static SharedObjectService getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * Pushes a job to the scheduler for single execution.
	 * 
	 * @param job
	 */
	public static void submitJob(IScheduledJob job) {
		schedulingService.addScheduledOnceJob(10, job);
	}

	/**
	 * @param maximumEventsPerUpdate the maximumEventsPerUpdate to set
	 */
	public void setMaximumEventsPerUpdate(int maximumEventsPerUpdate) {
		MAXIMUM_EVENTS_PER_UPDATE = maximumEventsPerUpdate;
	}

	/**
	 * Setter for persistence class name.
	 * 
	 * @param name Setter for persistence class name
	 */
	public void setPersistenceClassName(String name) {
		persistenceClassName = name;
	}

	/**
	 * Return scope store
	 * 
	 * @param scope Scope
	 * @param persistent Persistent store or not?
	 * @return Scope's store
	 */
	private IPersistenceStore getStore(IScope scope, boolean persistent) {
		IPersistenceStore store;
		if (!persistent) {
			// Use special store for non-persistent shared objects
			if (!scope.hasAttribute(SO_TRANSIENT_STORE)) {
				store = new RamPersistence(scope);
				scope.setAttribute(SO_TRANSIENT_STORE, store);
				return store;
			}
			return (IPersistenceStore) scope.getAttribute(SO_TRANSIENT_STORE);
		}
		// Evaluate configuration for persistent shared objects
		if (!scope.hasAttribute(SO_PERSISTENCE_STORE)) {
			try {
				store = PersistenceUtils.getPersistenceStore(scope, persistenceClassName);
				log.info("Created persistence store {} for shared objects.", store);
			} catch (Exception err) {
				log.warn("Could not create persistence store ({}) for shared objects, falling back to Ram persistence.", persistenceClassName, err);
				store = new RamPersistence(scope);
			}
			scope.setAttribute(SO_PERSISTENCE_STORE, store);
			return store;
		}
		return (IPersistenceStore) scope.getAttribute(SO_PERSISTENCE_STORE);
	}

	/** {@inheritDoc} */
	public boolean createSharedObject(IScope scope, String name, boolean persistent) {
		if (!hasSharedObject(scope, name)) {
			return scope.addChildScope(new SharedObjectScope(scope, name, persistent, getStore(scope, persistent)));
		}
		// the shared object already exists
		return true;
	}

	/** {@inheritDoc} */
	public ISharedObject getSharedObject(IScope scope, String name) {
		return (ISharedObject) scope.getBasicScope(TYPE, name);
	}

	/** {@inheritDoc} */
	public ISharedObject getSharedObject(IScope scope, String name, boolean persistent) {
		if (!hasSharedObject(scope, name)) {
			createSharedObject(scope, name, persistent);
		}
		return getSharedObject(scope, name);
	}

	/** {@inheritDoc} */
	public Set<String> getSharedObjectNames(IScope scope) {
		Set<String> result = new HashSet<String>();
		Iterator<String> iter = scope.getBasicScopeNames(TYPE);
		while (iter.hasNext()) {
			result.add(iter.next());
		}
		return result;
	}

	/** {@inheritDoc} */
	public boolean hasSharedObject(IScope scope, String name) {
		return scope.hasChildScope(TYPE, name);
	}

	/** {@inheritDoc} */
	public boolean clearSharedObjects(IScope scope, String name) {
		boolean result = false;
		if (hasSharedObject(scope, name)) {
			// '/' clears all local and persistent shared objects associated with the instance
			// /foo/bar clears the shared object /foo/bar; if bar is a directory name, no shared objects are deleted.
			// /foo/bar/* clears all shared objects stored under the instance directory /foo/bar. 
			// The bar directory is also deleted if no persistent shared objects are in use within this namespace.
			// /foo/bar/XX?? clears all shared objects that begin with XX, followed by any two characters. If a directory name matches
			// this specification, all the shared objects within this directory are cleared.
			result = ((ISharedObject) scope.getBasicScope(TYPE, name)).clear();
		}
		return result;
	}

	public void destroy() throws Exception {
	
	}
}
