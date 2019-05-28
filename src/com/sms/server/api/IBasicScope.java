package com.sms.server.api;

import com.sms.server.api.event.IEventObservable;
import com.sms.server.api.persistence.IPersistable;


public interface IBasicScope extends ICoreObject, IEventObservable,
Iterable<IBasicScope>, IPersistable{

	/**
	 * Does this scope have a parent? You can think of scopes as of tree items
	 * where scope may have a parent and children (child).
	 * 
	 * @return <code>true</code> if this scope has a parent, otherwise
	 *         <code>false</code>
	 */
	public boolean hasParent();

	/**
	 * Get this scopes parent.
	 * 
	 * @return parent scope, or <code>null</code> if this scope doesn't have a
	 *         parent
	 */
	public IScope getParent();

	/**
	 * Get the scopes depth, how far down the scope tree is it. The lowest depth
	 * is 0x00, the depth of Global scope. Application scope depth is 0x01. Room
	 * depth is 0x02, 0x03 and so forth.
	 * 
	 * @return the depth
	 */
	public int getDepth();

	/**
	 * Get the name of this scope. Eg. <code>someroom</code>.
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Get the full absolute path. Eg. <code>host/myapp/someroom</code>.
	 * 
	 * @return Absolute scope path
	 */
	public String getPath();

	/**
	 * Get the type of the scope.
	 * 
	 * @return Type of scope
	 */
	public String getType();
	
	/**
	 * Sets the amount of time to keep the scope available after the
	 * last disconnect.
	 * 
	 * @param keepDelay delay
	 */
	public void setKeepDelay(int keepDelay);
}
