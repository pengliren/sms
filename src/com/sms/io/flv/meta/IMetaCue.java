package com.sms.io.flv.meta;


/**
 * ICuePoint defines contract methods for use with 
 * cuepoints
 */
public interface IMetaCue extends IMeta, Comparable<Object> {

	/**
	 * Sets the name
	 * 
	 * @param name          Cue point name
	 * 
	 */
	public void setName(String name);

	/**
	 * Gets the name
	 * 
	 * @return name         Cue point name
	 * 
	 */
	public String getName();

	/**
	 * Sets the type type can be "event" or "navigation"
	 * 
	 * @param type          Cue point type
	 *
	 */
	public void setType(String type);

	/**
	 * Gets the type
	 * 
	 * @return type         Cue point type
	 *
	 */
	public String getType();

	/**
	 * Sets the time
	 * 
	 * @param d              Timestamp
	 *
	 */
	public void setTime(double d);

	/**
	 * Gets the time
	 * 
	 * @return time          Timestamp
	 *
	 */
	public double getTime();
}
