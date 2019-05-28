package com.sms.server.plugin;

import java.util.Map;

public final class PluginDescriptor {

	private String pluginName;

	private String pluginType;

	private String method;

	private String methodReturnType;
	
	private Map<String, Object> properties;

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getPluginType() {
		return pluginType;
	}

	public void setPluginType(String pluginType) {
		this.pluginType = pluginType;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethodReturnType() {
		return methodReturnType;
	}

	public void setMethodReturnType(String methodReturnType) {
		this.methodReturnType = methodReturnType;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
}
