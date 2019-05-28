package com.sms.server.stream.proxy;

import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;

/**
 * Proxy Stream Factory
 * @author pengliren
 *
 */
public class ProxyStreamFactory {

	public static BaseRTMPProxyStream createProxyStream(String uri, String localStreamName) throws URISyntaxException {
		
		if(StringUtils.isEmpty(uri)) {
			return null;
		} else {
			if(StringUtils.startsWithIgnoreCase(uri, "http://")) {
				return new HTTPPullProxyStream(uri, localStreamName);
			} else if(StringUtils.startsWithIgnoreCase(uri, "rtmp://")) {
				return new RTMPProxyStream(uri, localStreamName);
			} else {
				return null;
			}
		}
	}
}
