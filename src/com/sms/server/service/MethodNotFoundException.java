package com.sms.server.service;

import java.util.Arrays;

/**
 * Thrown if service method is not found so call throws exception
 */
public class MethodNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7559230924102506068L;

    /**
     * Creates exception with given method name
     * @param methodName      Service method name that can't be found
     */
    public MethodNotFoundException(String methodName) {
		super("Method " + methodName + " without arguments not found");
	}

    /**
     * Creates exception with given method name and arguments
     * @param methodName      Service method name that can't be found
     * @param args			  Arguments given
     */
    public MethodNotFoundException(String methodName, Object[] args) {
		super("Method " + methodName + " with arguments " + Arrays.asList(args) + " not found");
	}

}
