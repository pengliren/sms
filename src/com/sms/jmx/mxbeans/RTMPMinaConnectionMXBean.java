package com.sms.jmx.mxbeans;

import java.util.List;
import java.util.Map;

import javax.management.MXBean;

@MXBean
public interface RTMPMinaConnectionMXBean {

	public String getType();

	public String getHost();

	public String getRemoteAddress();

	public List<String> getRemoteAddresses();

	public int getRemotePort();

	public String getPath();

	public String getSessionId();

	public Map<String, Object> getConnectParams();

	public boolean isConnected();

	public void close();

	public long getReadBytes();

	public long getWrittenBytes();

	public long getReadMessages();

	public long getWrittenMessages();

	public long getDroppedMessages();

	public long getPendingMessages();

	public long getPendingVideoMessages(int streamId);
	
    public void invokeMethod(String method);

}
