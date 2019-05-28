package com.sms.server.net.udp;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multicast Incoming
 * @author pengliren
 *
 */
public class MulticastIncoming implements IUDPTransportIncoming {

	private static final Logger log = LoggerFactory.getLogger(MulticastIncoming.class);
	
	private List<MulticastIncomingConnection> connections = new ArrayList<MulticastIncomingConnection>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private long connectionCount = 0L;
	
	@Override
	public IUDPTransportIncomingConnection bind(UDPDatagramConfig config, IUDPMessageHandler messageHandler, String ipAddress, int port) {

		MulticastIncomingConnection connection = null;
		lock.writeLock().lock();
		try {
			MulticastListener multicastListener = new MulticastListener();
			multicastListener.bind(config, messageHandler, ipAddress, port);
			connection = new MulticastIncomingConnection(this, multicastListener);
			connections.add(connection);
			connectionCount++;
			multicastListener.setDaemon(true);
			multicastListener.setPriority(Thread.MAX_PRIORITY); // set thread priority
			multicastListener.start();
		} catch (Exception e) {
			log.error("bind: {}", e.toString());
		} finally {
			lock.writeLock().unlock();
		}
		return connection;
	}

	@Override
	public void unbind(IUDPTransportIncomingConnection connection) {
		MulticastIncomingConnection multicastIncomingConn = null;
		lock.writeLock().lock();
		try {
			if (connection instanceof MulticastIncomingConnection && connections.contains(connection)) {
				multicastIncomingConn = (MulticastIncomingConnection) connection;
				connectionCount -= 1L;
				connections.remove(multicastIncomingConn);
			}

			if (multicastIncomingConn != null) {
				multicastIncomingConn.isOpen = false;
				multicastIncomingConn.listener.unbind();
			}
		} catch (Exception e) {
			log.error("unbind: {}", e.toString());
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public long getConnectionCount() {

		long count = 0;
		lock.readLock().lock();
		try {
			count = this.connectionCount;
		} finally {
			lock.readLock().unlock();
		}
		return count;
	}

	public static void main(String[] args) {
		
		MulticastIncoming incoming = new MulticastIncoming();
		UDPDatagramConfig config = new UDPDatagramConfig();
		config.setReadBufferSize(4096);
		config.setReceiveBufferSize(4096);
		IUDPTransportIncomingConnection conn = incoming.bind(config, new IUDPMessageHandler() {
			
			@Override
			public void sessionOpened(IUDPTransportSession session) {
				System.out.println("session open");
			}
			
			@Override
			public void sessionClosed(IUDPTransportSession session) {
				System.out.println("session close");
			}
			
			@Override
			public void handleMessage(SocketAddress address, IoBuffer buffer) {
				System.out.println("message handle: "+ buffer.remaining());
			}
		}, "225.0.0.1", 1234);
		
		
		incoming.unbind(conn);
	}
}
