package com.sms.server.messaging;

import java.io.Serializable;
import java.util.Map;

/**
 * Out-of-band control message used by inter-components communication
 * which are connected with pipes.
 * Out-of-band data is a separate data stream used for specific purposes (in TCP
 * it's referenced as "urgent data"), like lifecycle control.
 *
 * <tt>'Target'</tt> is used to represent the receiver who may be
 * interested for receiving. It's a string of any form.
 * XXX shall we design a standard form for Target, like "class.instance"?
 */
public class OOBControlMessage implements Serializable {
	private static final long serialVersionUID = -6037348177653934300L;
    /**
     * Target
     */
	private String target;
    /**
     * Service name
     */
	private String serviceName;
    /**
     * Service params name
     */
	private Map<String, Object> serviceParamMap;
    /**
     * Result
     */
	private Object result;

	/**
     * Getter for property 'serviceName'.
     *
     * @return Value for property 'serviceName'.
     */
    public String getServiceName() {
		return serviceName;
	}

	/**
     * Setter for property 'serviceName'.
     *
     * @param serviceName Value to set for property 'serviceName'.
     */
    public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
     * Getter for property 'serviceParamMap'.
     *
     * @return Value for property 'serviceParamMap'.
     */
    public Map<String, Object> getServiceParamMap() {
		return serviceParamMap;
	}

	/**
     * Setter for property 'serviceParamMap'.
     *
     * @param serviceParamMap Value to set for property 'serviceParamMap'.
     */
    public void setServiceParamMap(Map<String, Object> serviceParamMap) {
		this.serviceParamMap = serviceParamMap;
	}

	/**
     * Getter for property 'target'.
     *
     * @return Value for property 'target'.
     */
    public String getTarget() {
		return target;
	}

	/**
     * Setter for property 'target'.
     *
     * @param target Value to set for property 'target'.
     */
    public void setTarget(String target) {
		this.target = target;
	}

	/**
     * Getter for property 'result'.
     *
     * @return Value for property 'result'.
     */
    public Object getResult() {
		return result;
	}

	/**
     * Setter for property 'result'.
     *
     * @param result Value to set for property 'result'.
     */
    public void setResult(Object result) {
		this.result = result;
	}
}
