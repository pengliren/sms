package com.sms.server.net.udp;

import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.util.CustomizableThreadFactory;

/**
 * MulticastOutgoing
 * @author pengliren
 *
 */
public class MulticastOutgoing implements IUDPTransportOutgoing {

	private static final Logger log = LoggerFactory.getLogger(MulticastOutgoing.class);
	
	public static int MINCONNECTIONS = 20;
	private static Executor executor = null;
	private ConnectorHolder[] connectors;
	private List<MulticastOutgoingConnection> connections = new ArrayList<MulticastOutgoingConnection>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private long connectionCount;
	  
	public void init(int processorCount) {
		this.connectors = new ConnectorHolder[processorCount];
		
		if(executor == null) {
			executor = Executors.newFixedThreadPool(processorCount, new CustomizableThreadFactory("MulticastWorkerExecutor-"));
		}
	}
	
	private int getNextConnector() {
		int nextIdx = 0;
		int count = 0;
		for (int i = 0; i < connectors.length; i++) {
			if (connectors[i] == null) {
				connectors[i] = new ConnectorHolder(new UDPSender(executor));
				nextIdx = i;
				break;
			}
			if (connectors[i].count < MINCONNECTIONS) {
				nextIdx = i;
				break;
			}
			if (connectors[i].count >= count)
				continue;
			count = connectors[i].count;
			nextIdx = i;
		}
		return nextIdx;
	}
	
	@Override
	public IUDPTransportOutgoingConnection connect(UDPDatagramConfig config, InetSocketAddress address) {
		MulticastOutgoingConnection connection = null;
		MulticastSocket multicastSocket = null;
		lock.writeLock().lock();
		try {
			int nextConnectorIdx = getNextConnector();
			ConnectorHolder connectorHolder = this.connectors[nextConnectorIdx];
			multicastSocket = new MulticastSocket(address.getPort());
			multicastSocket.setTrafficClass(config.getTrafficClass());
			multicastSocket.setReceiveBufferSize(config.getReceiveBufferSize());
			multicastSocket.setSendBufferSize(config.getSendBufferSize());
			multicastSocket.setReuseAddress(config.isReuseAddress());
			multicastSocket.setSoTimeout(config.getMulticastTimeout());
			multicastSocket.joinGroup(address.getAddress());
			connection = new MulticastOutgoingConnection(this, multicastSocket, address, connectorHolder.sender, nextConnectorIdx);
			connections.add(connection);
			connectorHolder.count++;
			connectionCount++;
		} catch (Exception e) {
			log.error("connect: {}", e.toString());
		} finally {
			lock.writeLock().unlock();
		}
		
		if (connection == null) {
			if (multicastSocket != null) {
				try {
					multicastSocket.close();
				} catch (Exception e) {
					log.error("close: {}", e.toString());
				}
			}
			multicastSocket = null;
		}
		return connection;
	}

	@Override
	public IUDPTransportOutgoingConnection connect(UDPDatagramConfig config, String host, int port) {

		InetSocketAddress address = new InetSocketAddress(host, port);
		return this.connect(config, address);
	}

	@Override
	public void disconnect(IUDPTransportOutgoingConnection connection) {
		
		MulticastOutgoingConnection tempConnection = null;
		MulticastSocket multicastSocket = null;
		lock.writeLock().lock();
		try {
			if (connection instanceof MulticastOutgoingConnection && connections.contains(connection)) {
				tempConnection = (MulticastOutgoingConnection) connection;
				int idx = tempConnection.connectorIndex;
				ConnectorHolder localConnectorHolder = this.connectors[idx];
				localConnectorHolder.count -= 1;
				this.connectionCount -= 1L;
				multicastSocket = tempConnection.socket;
				this.connections.remove(tempConnection);
			}
		} catch (Exception e) {
			log.error("disconnect: {}", e.toString());
		} finally {
			lock.writeLock().unlock();
		}
		
		if (tempConnection != null) {
			synchronized (tempConnection) {
				tempConnection.isOpen = false;
				if (multicastSocket != null) {
					try {
						multicastSocket.close();
					} catch (Exception e) {
						log.error("multicastSocket close: {}", e.toString());
					}
				}
				multicastSocket = null;
			}
		}
	}

	@Override
	public long getConnectionCount() {
		
		lock.readLock().lock();
		long count = 0;
		try {
			count = connectionCount;
		} finally {
			lock.readLock().unlock();
		}
		return count;
	}

	class ConnectorHolder {
		public int count = 0;
		public UDPSender sender;

		public ConnectorHolder(UDPSender sender) {
			this.sender = sender;
		}
	}
}
