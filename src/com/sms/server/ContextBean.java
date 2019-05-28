package com.sms.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContextBean {

	private String className;
	private Map<String, String> propertyMap = new ConcurrentHashMap<String, String>();

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getProperty(String proName) {

		return propertyMap.get(proName);
	}

	public void setProperty(String proName, String proValue) {

		propertyMap.put(proName, proValue);
	}
	
	public Map<String, String> getPropertyMap() {
		
		return propertyMap;
	}

}
