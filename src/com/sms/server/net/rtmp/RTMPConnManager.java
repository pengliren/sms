package com.sms.server.net.rtmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.BaseConnection;

public class RTMPConnManager implements IRTMPConnManager {

	private static final Logger log = LoggerFactory.getLogger(RTMPConnManager.class);

	private ConcurrentMap<Integer, RTMPConnection> connMap = new ConcurrentHashMap<Integer, RTMPConnection>();

	private ReadWriteLock lock = new ReentrantReadWriteLock();

	public RTMPConnection createConnection(Class<?> connCls) {
		RTMPConnection conn = null;
		if (RTMPConnection.class.isAssignableFrom(connCls)) {
			try {
				conn = createConnectionInstance(connCls);
				lock.writeLock().lock();
				try {
					int clientId = BaseConnection.getNextClientId();
					conn.setId(clientId);
					connMap.put(clientId, conn);
					log.debug("Connection created, id: {}", conn.getId());
				} finally {
					lock.writeLock().unlock();
				}
			} catch (Exception e) {
			}
		}
		return conn;
	}

	public RTMPConnection getConnection(int clientId) {
		lock.readLock().lock();
		try {
			return connMap.get(clientId);
		} finally {
			lock.readLock().unlock();
		}
	}

	public RTMPConnection removeConnection(int clientId) {
		lock.writeLock().lock();
		try {
			log.debug("Removing connection with id: {}", clientId);
			return connMap.remove(clientId);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public Collection<RTMPConnection> removeConnections() {
		ArrayList<RTMPConnection> list = new ArrayList<RTMPConnection>(connMap.size());
		lock.writeLock().lock();
		try {
			list.addAll(connMap.values());
			return list;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public RTMPConnection createConnectionInstance(Class<?> cls) throws Exception {
		RTMPConnection conn = (RTMPConnection)cls.newInstance();
		return conn;
	}
}
