package com.sms.jmx.mxbeans;

import java.io.IOException;

import javax.management.MXBean;

import com.sms.server.api.stream.ResourceExistException;
import com.sms.server.api.stream.ResourceNotFoundException;
import com.sms.server.messaging.IProvider;

@MXBean
public interface ClientBroadcastStreamMXBean {

	public void start();

	public void startPublishing();

	public void stop();

	public void close();

	public void saveAs(String name, boolean isAppend) throws IOException,
			ResourceNotFoundException, ResourceExistException;

	public String getSaveFilename();

	public IProvider getProvider();

	public String getPublishedName();

	public void setPublishedName(String name);
	
	public long getBytesReceived();
	
	public int getMaxSubscribers();
	
	public int getActiveSubscribers();
}
