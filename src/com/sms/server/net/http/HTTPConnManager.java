package com.sms.server.net.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Conn Manager
 * @author pengliren
 *
 */
public class HTTPConnManager implements IHTTPConnManager {

	private static final Logger log = LoggerFactory.getLogger(HTTPConnManager.class);

	private ConcurrentMap<Long, HTTPMinaConnection> connMap = new ConcurrentHashMap<Long, HTTPMinaConnection>();

	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private static final class SingletonHolder {

		private static final HTTPConnManager INSTANCE = new HTTPConnManager();
	}

	protected HTTPConnManager() {

	}

	public static HTTPConnManager getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public HTTPMinaConnection getConnection(long clientId) {
	
		lock.readLock().lock();
		try {
			return connMap.get(clientId);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void addConnection(HTTPMinaConnection conn, long clientId) {
		
		lock.writeLock().lock();
		try {
			log.debug("add connection with id: {}", clientId);
			connMap.put(clientId, conn);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public HTTPMinaConnection removeConnection(long clientId) {
		
		lock.writeLock().lock();
		try {
			log.debug("Removing connection with id: {}", clientId);
			return connMap.remove(clientId);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public Collection<HTTPMinaConnection> removeConnections() {
		
		ArrayList<HTTPMinaConnection> list = new ArrayList<HTTPMinaConnection>(connMap.size());
		lock.writeLock().lock();
		try {
			list.addAll(connMap.values());
			return list;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public int getConnectionCount() {

		lock.writeLock().lock();
		try {
			return connMap.size();
		} finally {
			lock.writeLock().unlock();
		}
	}

}
