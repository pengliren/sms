package com.sms.server.api.so;

import java.util.List;

import com.sms.server.api.IScope;

/**
 * Interface for handlers that control access to shared objects.
 * */
public interface ISharedObjectSecurity {

	/**
	 * Check if the a shared object may be created in the given scope.
	 * 
	 * @param scope scope
	 * @param name name
	 * @param persistent is persistent
	 * @return is creation allowed
	 */
	public boolean isCreationAllowed(IScope scope, String name, boolean persistent);

	/**
	 * Check if a connection to the given existing shared object is allowed.
	 * 
	 * @param so shared ojbect
	 * @return is connection alowed
	 */
	public boolean isConnectionAllowed(ISharedObject so);

	/**
	 * Check if a modification is allowed on the given shared object.
	 * 
	 * @param so shared object
	 * @param key key
	 * @param value value
	 * @return true if given key is modifiable; false otherwise
	 */
	public boolean isWriteAllowed(ISharedObject so, String key, Object value);

	/**
	 * Check if the deletion of a property is allowed on the given shared object.
	 * 
	 * @param so shared object
	 * @param key key
	 * @return true if delete allowed; false otherwise
	 */
	public boolean isDeleteAllowed(ISharedObject so, String key);

	/**
	 * Check if sending a message to the shared object is allowed.
	 * 
	 * @param so shared object
	 * @param message message
	 * @param arguments arguments
	 * @return true if allowed
	 */
	public boolean isSendAllowed(ISharedObject so, String message, List<?> arguments);
}
