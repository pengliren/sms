package com.sms.server.stream.proxy;

public interface IProxyStream {

	public void register();
	public boolean isClosed();
	public void stop();
}
