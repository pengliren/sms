package com.sms.server.api;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface ICastingAttributeStore extends IAttributeStore {

	/**
	 * Get Boolean attribute by name
	 * 
	 * @param name Attribute name
	 * @return		Attribute
	 */
	public Boolean getBoolAttribute(String name);

	/**
	 * Get Byte attribute by name
	 * 
	 * @param name Attribute name
	 * @return		Attribute
	 */
	public Byte getByteAttribute(String name);

	/**
	 * Get Double attribute by name
	 * 
	 * @param name Attribute name
	 * @return		Attribute
	 */
	public Double getDoubleAttribute(String name);

	/**
	 * Get Integer attribute by name
	 * 
	 * @param name Attribute name
	 * @return		Attribute
	 */
	public Integer getIntAttribute(String name);

	/**
	 * Get List attribute by name
	 * 
	 * @param name Attribute name
	 * @return		Attribute
	 */
	public List<?> getListAttribute(String name);

	/**
	 * Get boolean attribute by name
	 * 
	 * @param name Attribute name
	 * @return		Attribute
	 */
	public Long getLongAttribute(String name);

	/**
	 * Get Long attribute by name
	 * 
	 * @param name Attribute name
	 * @return		Attribute
	 */
	public Map<?, ?> getMapAttribute(String name);

	/**
	 * Get Set attribute by name
	 * 
	 * @param name Attribute name
	 * @return		Attribute
	 */
	public Set<?> getSetAttribute(String name);

	/**
	 * Get Short attribute by name
	 * 
	 * @param name Attribute name
	 * @return		Attribute
	 */
	public Short getShortAttribute(String name);

	/**
	 * Get String attribute by name
	 * 
	 * @param name Attribute name
	 * @return		Attribute
	 */
	public String getStringAttribute(String name);
}
