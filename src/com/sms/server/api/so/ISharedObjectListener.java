package com.sms.server.api.so;

import java.util.List;
import java.util.Map;

import com.sms.server.api.IAttributeStore;

/**
 * Notifications about shared object updates.
 */
public interface ISharedObjectListener {

	/**
	 * Called when a client connects to a shared object.
	 * 
	 * @param so
	 *            the shared object
	 */
	void onSharedObjectConnect(ISharedObjectBase so);

	/**
	 * Called when a client disconnects from a shared object.
	 * 
	 * @param so
	 *            the shared object
	 */
	void onSharedObjectDisconnect(ISharedObjectBase so);

	/**
	 * Called when a shared object attribute is updated.
	 * 
	 * @param so
	 *            the shared object
	 * @param key
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	void onSharedObjectUpdate(ISharedObjectBase so, String key, Object value);

	/**
	 * Called when multiple attributes of a shared object are updated.
	 * 
	 * @param so
	 *            the shared object
	 * @param values
	 *            the new attributes of the shared object
	 */
	void onSharedObjectUpdate(ISharedObjectBase so, IAttributeStore values);

	/**
	 * Called when multiple attributes of a shared object are updated.
	 * 
	 * @param so
	 *            the shared object
	 * @param values
	 *            the new attributes of the shared object
	 */
	void onSharedObjectUpdate(ISharedObjectBase so, Map<String, Object> values);

	/**
	 * Called when an attribute is deleted from the shared object.
	 * 
	 * @param so
	 *            the shared object
	 * @param key
	 *            the name of the attribute to delete
	 */
	void onSharedObjectDelete(ISharedObjectBase so, String key);

	/**
	 * Called when all attributes of a shared object are removed.
	 * 
	 * @param so
	 *            the shared object
	 */
	void onSharedObjectClear(ISharedObjectBase so);

	/**
	 * Called when a shared object method call is sent.
	 * 
	 * @param so
	 *            the shared object
	 * @param method
	 *            the method name to call
	 * @param params
	 *            the arguments
	 */
	void onSharedObjectSend(ISharedObjectBase so, String method, List<?> params);
}
