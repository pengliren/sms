package com.sms.server.api.stream.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IScope;
import com.sms.server.api.stream.IServerStream;
import com.sms.server.stream.ServerStream;

/**
 * Stream helper methods.)
 */
public abstract class StreamUtils {

	private static final Logger logger = LoggerFactory.getLogger(StreamUtils.class);

	/* Map to hold reference to the instanced server streams */
	private static volatile ConcurrentMap<String, IServerStream> serverStreamMap = new ConcurrentHashMap<String, IServerStream>();

	/**
	 * Creates server stream
	 * 
	 * @param scope Scope of stream
	 * @param name Name of stream
	 * @return		IServerStream object
	 */
	public static IServerStream createServerStream(IScope scope, String name) {
		logger.debug("Creating server stream: {} scope: {}", name, scope);
		ServerStream stream = new ServerStream();
		stream.setScope(scope);
		stream.setName(name);
		stream.setPublishedName(name);
		//save to the list for later lookups
		String key = scope.getName() + '/' + name;
		serverStreamMap.put(key, stream);
		return stream;
	}

	/**
	 * Looks up a server stream in the stream map. Null will be returned if the 
	 * stream is not found.
	 *
	 * @param scope Scope of stream
	 * @param name Name of stream
	 * @return		IServerStream object
	 */
	public static IServerStream getServerStream(IScope scope, String name) {
		logger.debug("Looking up server stream: {} scope: {}", name, scope);
		String key = scope.getName() + '/' + name;
		if (serverStreamMap.containsKey(key)) {
			return serverStreamMap.get(key);
		} else {
			logger.warn("Server stream not found with key: {}", key);
			return null;
		}
	}
	
	/**
	 * Puts a server stream in the stream map
	 *
	 * @param scope Scope of stream
	 * @param name Name of stream
	 * @param stream ServerStream object
	 */
	public static void putServerStream(IScope scope, String name, IServerStream stream) {
		logger.debug("Putting server stream in the map - name: {} scope: {} stream: {}", new Object[]{name, scope, stream});
		String key = scope.getName() + '/' + name;
		if (!serverStreamMap.containsKey(key)) {
			serverStreamMap.put(key, stream);
		} else {
			logger.warn("Server stream already exists in the map with key: {}", key);
		}
	}
	
	/**
	 * Removes a server stream from the stream map
	 *
	 * @param scope Scope of stream
	 * @param name Name of stream
	 */
	public static void removeServerStream(IScope scope, String name) {
		logger.debug("Removing server stream from the map - name: {} scope: {}", name, scope);
		String key = scope.getName() + '/' + name;
		if (serverStreamMap.containsKey(key)) {
			serverStreamMap.remove(key);
		} else {
			logger.warn("Server stream did not exist in the map with key: {}", key);
		}
	}	
	
}
