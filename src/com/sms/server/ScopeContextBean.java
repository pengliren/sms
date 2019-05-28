package com.sms.server;

import java.util.HashMap;
import java.util.Map;

public class ScopeContextBean {

	public final static String RTMPAPPLICATIONADAPTER_BEAN = "RtmpApplicationAdapter";
	public final static String HTTPAPPLICATIONADAPTER_BEAN = "HttpApplicationAdapter";
	public final static String BROADCASTSTREAM_BEAN = "BroadcastStream";
	public final static String SUBSCRIBERSTREAM_BEAN = "SubscriberStream";
	public final static String SINGLESTREAM_BEAN = "SingleStream";
	public final static String PROVIDERSERVICE_BEAN = "ProviderService";
	public final static String CONSUMERSERVICE_BEAN = "ConsumerService";
	public final static String STREAMSERVICE_BEAN = "StreamService";
	public final static String FILECONSUMER_BEAN = "FileConsumer";
	public final static String RTMPSAMPLEACCESS_BEAN = "RtmpSampleAccess";
	public final static String SECURITYFORBIDDEN_BEAN = "Forbidden";

	private Map<String, ContextBean> clazzMap = new HashMap<String, ContextBean>();
	
	public void addClazz(String name, ContextBean bean) {
		
		clazzMap.put(name, bean);
	}
	
	public ContextBean getClazz(String name) {
		
		return clazzMap.get(name);
	}

	public Map<String, ContextBean> getClazzMap() {
		return clazzMap;
	}

	public void setClazzMap(Map<String, ContextBean> clazzMap) {
		this.clazzMap = clazzMap;
	}
}
