package com.sms.server.api;

import java.util.Map;
import java.util.Set;

public interface IAttributeStore {

	/**
	 * Get the attribute names. The resulting set will be read-only.
	 * 
	 * @return set containing all attribute names
	 */
	public Set<String> getAttributeNames();

	/**
	 * Get the attributes. The resulting map will be read-only.
	 * 
	 * @return map containing all attributes
	 */
	public Map<String, Object> getAttributes();
	
	/**
	 * Set an attribute on this object.
	 * 
	 * @param name the name of the attribute to change
	 * @param value the new value of the attribute
	 * @return true if the attribute value changed otherwise false
	 */
	public boolean setAttribute(String name, Object value);

	/**
	 * Set multiple attributes on this object.
	 * 
	 * @param values the attributes to set
	 */
	public boolean setAttributes(Map<String, Object> values);

	/**
	 * Set multiple attributes on this object.
	 * 
	 * @param values the attributes to set
	 */
	public boolean setAttributes(IAttributeStore values);

	/**
	 * Return the value for a given attribute.
	 * 
	 * @param name the name of the attribute to get
	 * @return the attribute value or null if the attribute doesn't exist
	 */
	public Object getAttribute(String name);

	/**
	 * Return the value for a given attribute and set it if it doesn't exist.
	 * 
	 * <p>
	 * This is a utility function that internally performs the following code:
	 * <p>
	 * <code>
	 * if (!hasAttribute(name)) setAttribute(name, defaultValue);<br>
	 * return getAttribute(name);<br>
	 * </code>
	 * </p>
	 * </p>
	 * 
	 * @param name the name of the attribute to get
	 * @param defaultValue the value of the attribute to set if the attribute doesn't
	 *            exist
	 * @return the attribute value
	 */
	public Object getAttribute(String name, Object defaultValue);

	/**
	 * Check the object has an attribute.
	 * 
	 * @param name the name of the attribute to check
	 * @return true if the attribute exists otherwise false
	 */
	public boolean hasAttribute(String name);

	/**
	 * Remove an attribute.
	 * 
	 * @param name the name of the attribute to remove
	 * @return true if the attribute was found and removed otherwise false
	 */
	public boolean removeAttribute(String name);

	/**
	 * Remove all attributes.
	 */
	public void removeAttributes();
	
	/**
	 * Size of the attribute store.
	 * 
	 * @return count of attributes
	 */
	public int size();
}
