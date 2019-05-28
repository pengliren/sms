package com.sms.server.api.so;

import java.util.Set;

import com.sms.server.api.IScope;
import com.sms.server.api.IScopeService;

/**
 * Service that manages shared objects for given scope.
 * 
 */
public interface ISharedObjectService extends IScopeService {

	public static String BEAN_NAME = "sharedObjectService";

	/**
	 * Get a set of the shared object names.
	 * 
	 * @param scope the scope to return the shared object names from
	 * @return set containing the shared object names
	 */
	public Set<String> getSharedObjectNames(IScope scope);

	/**
	 * Create a new shared object.
	 * 
	 * @param scope the scope to create the shared object in
	 * @param name the name of the shared object
	 * @param persistent will the shared object be persistent
	 * @return <code>true</code> if the shared object was created, otherwise
	 *         <code>false</code>
	 */
	public boolean createSharedObject(IScope scope, String name, boolean persistent);

	/**
	 * Get a shared object by name.
	 * 
	 * @param scope the scope to get the shared object from
	 * @param name the name of the shared object
	 * @return shared object, or <code>null</code> if not found
	 */
	public ISharedObject getSharedObject(IScope scope, String name);

	/**
	 * Get a shared object by name and create it if it doesn't exist.
	 * 
	 * @param scope the scope to get the shared object from
	 * @param name the name of the shared object
	 * @param persistent should the shared object be created persistent 
	 * @return the shared object
	 */
	public ISharedObject getSharedObject(IScope scope, String name, boolean persistent);

	/**
	 * Check if a shared object exists.
	 * 
	 * @param scope the scope to check for the shared object
	 * @param name the name of the shared object
	 * @return <code>true</code> if the shared object exists, otherwise
	 *         <code>false</code>
	 */
	public boolean hasSharedObject(IScope scope, String name);

	/**
	 * <p>
	 * Deletes persistent shared objects specified by name and clears all
	 * properties from active shared objects (persistent and nonpersistent). The
	 * name parameter specifies the name of a shared object, which can include a
	 * slash (/) as a delimiter between directories in the path. The last
	 * element in the path can contain wildcard patterns (for example, a
	 * question mark [?] and an asterisk [*]) or a shared object name. The
	 * clearSharedObjects() method traverses the shared object hierarchy along
	 * the specified path and clears all the shared objects. Specifying a slash
	 * (/) clears all the shared objects associated with an application
	 * instance.
	 * </p>
	 * <p>
	 * The following values are possible for the soPath parameter: <br /> /
	 * clears all local and persistent shared objects associated with the
	 * instance. <br />
	 * /foo/bar clears the shared object /foo/bar; if bar is a directory name,
	 * no shared objects are deleted. <br />
	 * /foo/bar/* clears all shared objects stored under the instance directory
	 * /foo/bar. The bar directory is also deleted if no persistent shared
	 * objects are in use within this namespace. <br />
	 * /foo/bar/XX?? clears all shared objects that begin with XX, followed by
	 * any two characters. If a directory name matches this specification, all
	 * the shared objects within this directory are cleared.
	 * </p>
	 * <p>
	 * If you call the clearSharedObjects() method and the specified path
	 * matches a shared object that is currently active, all its properties are
	 * deleted, and a "clear" event is sent to all subscribers of the shared
	 * object. If it is a persistent shared object, the persistent store is also
	 * cleared.
	 * </p>
	 * <br />
	 * 
	 * @param scope the scope to check for the shared object
	 * @param name the name of the shared object
	 * @return true if the shared object at the specified path was deleted;
	 *         otherwise, false. If using wildcard characters to delete multiple
	 *         files, the method returns true only if all the shared objects
	 *         matching the wildcard pattern were successfully deleted;
	 *         otherwise, it will return false.
	 */
	public boolean clearSharedObjects(IScope scope, String name);
}
