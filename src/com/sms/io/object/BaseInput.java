package com.sms.io.object;


import java.util.HashMap;
import java.util.Map;

/**
 * BaseInput represents a way to map input to a HashMap.  This class
 * is meant to be extended.
 */
public class BaseInput {
	
	/**
     * References map
     */
	protected Map<Integer, Object> refMap = new HashMap<Integer, Object>();

    /**
     * References id
     */
    protected int refId;
    
	/**
	 * Store an object into a map
	 * @param obj  Object to store
	 */
	protected int storeReference(Object obj) {
		int newRefId = refId++;
		refMap.put(Integer.valueOf(newRefId), obj);
		return newRefId;
	}

	/**
	 * Replace a referenced object with another one. This is used
	 * by the AMF3 deserializer to handle circular references.
	 * 
	 * @param refId
	 * @param newRef
	 */
	protected void storeReference(int refId, Object newRef) {
		refMap.put(Integer.valueOf(refId), newRef);
	}
	
	/**
	 * Clears the map
	 */
	public void clearReferences() {
		refMap.clear();
		refId = 0;
	}

	/**
	 * Returns the object with the parameters id
	 * @param id        Object reference id
	 * @return Object   Object reference with given id
	 */
	protected Object getReference(int id) {
		return refMap.get(Integer.valueOf(id));
	}

}
