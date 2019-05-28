package com.sms.io.object;

import java.util.HashMap;
import java.util.Map;

/**
 * BaseOutput represents a way to map input to a HashMap.  This class
 * is meant to be extended.
 */
public class BaseOutput {

	static class IdentityWrapper {
        /**
         * Wrapped object
         */
		Object object;

        /**
         * Creates wrapper for object
         * @param object        Object to wrap
         */
        public IdentityWrapper(Object object) {
			this.object = object;
		}

		/** {@inheritDoc} */
        @Override
		public int hashCode() {
			return System.identityHashCode(object);
		}

		/** {@inheritDoc} */
        @Override
		public boolean equals(Object object) {
			if (object instanceof IdentityWrapper) {
				return ((IdentityWrapper) object).object == this.object;
			}

			return false;
		}

	}

    /**
     * References map
     */
    protected Map<IdentityWrapper, Short> refMap;

    /**
     * Reference id
     */
    protected short refId;

	/**
	 * BaseOutput Constructor
	 *
	 */
	protected BaseOutput() {
		refMap = new HashMap<IdentityWrapper, Short>();
	}

	/**
	 * Store an object into a map
	 * 
	 * @param obj   Object to store
	 */
	protected void storeReference(Object obj) {
		refMap.put(new IdentityWrapper(obj), Short.valueOf(refId++));
	}

	/**
	 * Returns a boolean stating whether the map contains an object with
	 * that key
	 * @param obj            Object
	 * @return boolean       <code>true</code> if it does contain it, <code>false</code> otherwise
	 */
	protected boolean hasReference(Object obj) {
		return refMap.containsKey(new IdentityWrapper(obj));
	}

	/**
	 * Clears the map
	 */
	public void clearReferences() {
		refMap.clear();
		refId = 0;
	}

	/**
	 * Returns the reference id based on the parameter obj
	 * 
	 * @param obj            Object
	 * @return short         Reference id
	 */
	protected short getReferenceId(Object obj) {
		return refMap.get(new IdentityWrapper(obj)).shortValue();
	}

}
