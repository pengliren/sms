package com.sms.server.persistence;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sms.server.api.IScope;
import com.sms.server.api.persistence.IPersistable;
import com.sms.server.api.persistence.IPersistenceStore;

/**
 * Persistence implementation that stores the objects in memory.
 * This serves as default persistence if nothing has been configured.
 */
public class RamPersistence implements IPersistenceStore {

	/** This is used in the id for objects that have a name of <code>null</code> **/
	protected static final String PERSISTENCE_NO_NAME = "__null__";

	/**
	 * Map for persistable objects
	 */
	protected ConcurrentMap<String, IPersistable> objects = new ConcurrentHashMap<String, IPersistable>();

	/**
	 * Resource pattern resolver. Resolves resources from patterns, loads resources.
	 */
	protected IScope scope;


	/**
	 * Creates RAM persistence object from scope
	 * @param scope                Scope
	 */
	public RamPersistence(IScope scope) {
		
		this.scope = scope;
	}

	/**
	 * Get resource name from path
	 * @param id                   Object ID. The format of the object id is <type>/<path>/<objectName>.
	 * @return                     Resource name
	 */
	protected String getObjectName(String id) {
		// The format of the object id is <type>/<path>/<objectName>
		String result = id.substring(id.lastIndexOf('/') + 1);
		if (result.equals(PERSISTENCE_NO_NAME)) {
			result = null;
		}
		return result;
	}

	/**
	 * Get object path for given id and name
	 * @param id                   Object ID. The format of the object id is <type>/<path>/<objectName>
	 * @param name                 Object name
	 * @return                     Resource path
	 */
	protected String getObjectPath(String id, String name) {
		// The format of the object id is <type>/<path>/<objectName>
		id = id.substring(id.indexOf('/') + 1);
		if (id.charAt(0) == '/') {
			id = id.substring(1);
		}
		if (id.lastIndexOf(name) <= 0) {
			return id;
		}
		return id.substring(0, id.lastIndexOf(name) - 1);
	}

	/**
	 * Get object id
	 * @param object               Persistable object whose id is asked for
	 * @return                     Given persistable object id
	 */
	protected String getObjectId(IPersistable object) {
		// The format of the object id is <type>/<path>/<objectName>
		String result = object.getType();
		if (object.getPath().charAt(0) != '/') {
			result += '/';
		}
		result += object.getPath();
		if (!result.endsWith("/")) {
			result += '/';
		}
		String name = object.getName();
		if (name == null) {
			name = PERSISTENCE_NO_NAME;
		}
		if (name.charAt(0) == '/') {
			// "result" already ends with a slash
			name = name.substring(1);
		}
		return result + name;
	}

	/** {@inheritDoc} */
	public boolean save(IPersistable object) {
		final String key = getObjectId(object);
		objects.put(key, object);
		return true;
	}

	/** {@inheritDoc} */
	public IPersistable load(String name) {
		return objects.get(name);
	}

	/** {@inheritDoc} */
	public boolean load(IPersistable obj) {
		return obj.isPersistent();
	}

	/** {@inheritDoc} */
	public boolean remove(IPersistable object) {
		return remove(getObjectId(object));
	}

	/** {@inheritDoc} */
	public boolean remove(String name) {
		if (!objects.containsKey(name)) {
			return false;
		}
		objects.remove(name);
		return true;
	}

	/** {@inheritDoc} */
	public Set<String> getObjectNames() {
		return objects.keySet();
	}

	/** {@inheritDoc} */
	public Collection<IPersistable> getObjects() {
		return objects.values();
	}

	/** {@inheritDoc} */
	public void notifyClose() {
		objects.clear();
	}
}
