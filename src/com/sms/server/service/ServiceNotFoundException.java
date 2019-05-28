package com.sms.server.service;

/**
 * Thrown when service can't be found thus remote call throws an exception
 */
public class ServiceNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7543755414829244027L;

	/** Name of service that doesn't exist. */
	private String serviceName;
	
    /**
     * Creates new exception with service name
     * @param serviceName       Name of service that couldn't been found
     */
    public ServiceNotFoundException(String serviceName) {
		super("Service not found: " + serviceName);
		this.serviceName = serviceName;
	}

    /**
     * Get the name of the service that doesn't exist.
     * 
     * @return name of the service
     */
    public String getServiceName() {
    	return serviceName;
    }
    
}
