package com.sms.jmx.mxbeans;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.MXBean;
import javax.management.openmbean.CompositeData;

@MXBean
public interface ConnectionMXBean {

	public String getType();

	public boolean isConnected();

	public void close();

	public Map<String, Object> getConnectParams();

	public CompositeData getClient();

	public String getHost();

	public String getRemoteAddress();

	public List<String> getRemoteAddresses();

	public int getRemotePort();

	public String getPath();

	public String getSessionId();

	public long getReadBytes();

	public long getWrittenBytes();

	public long getReadMessages();

	public long getWrittenMessages();

	public long getDroppedMessages();

	public long getPendingMessages();

	public void ping();

	public int getLastPingTime();

	public CompositeData getScope();

	public Iterator<CompositeData> getBasicScopes();
}
