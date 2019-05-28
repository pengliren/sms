package com.sms.server.stream.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rtsp tcp push stream
 * @author pengliren
 *
 */
public class RTSPPushProxyStream extends RTSPProxyStream {

	private static Logger log = LoggerFactory.getLogger(RTSPPushProxyStream.class);
	
	public RTSPPushProxyStream(String streamName) {
		super(streamName);
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
			start = true;
			connManager.register(publishedName, this);
		}
		
		log.info("rtsp push proxy stream {} is start!", getPublishedName());
	}
}
