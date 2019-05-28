package com.sms.server.api.persistence;

import java.lang.reflect.Constructor;

/**
 * Helper class for persistence.
 */

public class PersistenceUtils {

	/**
	 * Returns persistence store object class constructor
	 * 
	 * @param theClass
	 *            Persistence store class
	 * @param interfaces
	 *            Interfaces that are being implemented by persistence store
	 *            object class
	 * @return				Constructor
	 * @throws Exception
	 */
	private static Constructor<?> getPersistenceStoreConstructor(Class<?> theClass,
			Class<?>[] interfaces) throws Exception {
		Constructor<?> constructor = null;
		for (Class<?> interfaceClass : interfaces) {
			try {
				constructor = theClass
						.getConstructor(new Class[] { interfaceClass });
			} catch (NoSuchMethodException err) {
				// Ignore this error
			}
			if (constructor != null) {
				break;
			}

			constructor = getPersistenceStoreConstructor(theClass,
					interfaceClass.getInterfaces());
			if (constructor != null) {
				break;
			}
		}
		return constructor;
	}

	/**
	 * Returns persistence store object. Persistence store is a special object
	 * that stores persistence objects and provides methods to manipulate them
	 * (save, load, remove, list).
	 * 
	 * @param resolver Resolves connection pattern into Resource object
	 * @param className Name of persistence class
	 * @return IPersistence store object that provides methods for persistence
	 *         object handling
	 * @throws Exception if error
	 */
	public static IPersistenceStore getPersistenceStore(
			Object resolver, String className)
			throws Exception {
		Class<?> persistenceClass = Class.forName(className);
		Constructor<?> constructor = getPersistenceStoreConstructor(
				persistenceClass, resolver.getClass().getInterfaces());
		if (constructor == null) {
			// Search in superclasses of the object.
			Class<?> superClass = resolver.getClass().getSuperclass();
			while (superClass != null) {
				constructor = getPersistenceStoreConstructor(persistenceClass,
						superClass.getInterfaces());
				if (constructor != null) {
					break;
				}

				superClass = superClass.getSuperclass();
			}
		}

		if (constructor == null) {
			throw new NoSuchMethodException();
		}

		return (IPersistenceStore) constructor
				.newInstance(new Object[] { resolver });
	}

}
