package com.sms.server.util;

public abstract class ClassUtil {

	/** Suffix for array class names: "[]" */

	public static final String ARRAY_SUFFIX = "[]";

	/** The package separator character '.' */

	private static final char PACKAGE_SEPARATOR = '.';

	/** The inner class separator character '$' */

	private static final char INNER_CLASS_SEPARATOR = '$';

	/** The CGLIB class separator character "$$" */

	public static final String CGLIB_CLASS_SEPARATOR = "$$";

	/** The ".class" file suffix */

	public static final String CLASS_FILE_SUFFIX = ".class";

	public static String getShortName(String className) {

		int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		int nameEndIndex = className.indexOf(CGLIB_CLASS_SEPARATOR);
		if (nameEndIndex == -1) {
			nameEndIndex = className.length();
		}
		String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
		shortName = shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
		return shortName;
	}

	public static String getShortName(Class<?> clazz) {

		return getShortName(getQualifiedName(clazz));
	}

	public static String getQualifiedName(Class<?> clazz) {

		if (clazz.isArray()) {
			return getQualifiedNameForArray(clazz);
		} else {
			return clazz.getName();
		}

	}

	private static String getQualifiedNameForArray(Class<?> clazz) {

		StringBuffer buffer = new StringBuffer();

		while (clazz.isArray()) {
			clazz = clazz.getComponentType();
			buffer.append(ClassUtil.ARRAY_SUFFIX);
		}

		buffer.insert(0, clazz.getName());

		return buffer.toString();

	}
}
