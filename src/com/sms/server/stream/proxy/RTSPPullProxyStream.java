package com.sms.server.stream.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.net.rtsp.RTSPClient;

/**
 * rtsp tcp pull stream
 * @author pengliren
 *
 */
public class RTSPPullProxyStream extends RTSPProxyStream {

	private static Logger log = LoggerFactory.getLogger(RTSPPullProxyStream.class);
	
	private final String url;
	
	private RTSPClient client;
	
	public RTSPPullProxyStream(String url, String streamName) {
		super(streamName);
		this.url = url;
		client = new RTSPClient(url, this);
	}
	
	@Override
	public void start() {
		
		if(getScope() == null) {
			throw new RuntimeException("scope is null!");
		}
		if(start) return;
		synchronized (lock) {
			super.start();
			register();
			client.startConnector();
			start = true;
			connManager.register(publishedName, this);
		}
		
		log.info("rtsp pull proxy stream {} is start!", getPublishedName());
	}
	
	@Override
	public void stop() {
		super.stop();
		client.disconnect();
	}

	public String getUrl() {
		return url;
	}
}
