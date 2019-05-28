package com.sms.server.stream.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * http 推流
 * @author pengliren
 *
 */
public class HTTPPushProxyStream extends HTTPProxyStream {

	private static Logger log = LoggerFactory.getLogger(HTTPPushProxyStream.class);
	
	public HTTPPushProxyStream(String streamName) {
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
		
		log.info("http push proxy stream {} is start!", getPublishedName());
	}
}
