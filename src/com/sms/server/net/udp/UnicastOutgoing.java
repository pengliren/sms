package com.sms.server.net.udp;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
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
 * UnicastOutgoing
 * @author pengliren
 *
 */
public class UnicastOutgoing implements IUDPTransportOutgoing {

	private static final Logger log = LoggerFactory.getLogger(UnicastOutgoing.class);
	public static int MINCONNECTIONS = 20;
	private static Executor executor = null;
	private ConnectorHolder[] connectors;
	private List<UnicastOutgoingConnection> connections = new ArrayList<UnicastOutgoingConnection>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private long connectionCount;
	  
	public void init(int processorCount) {
		this.connectors = new ConnectorHolder[processorCount];
		
		if(executor == null) {
			executor = Executors.newFixedThreadPool(processorCount, new CustomizableThreadFactory("UnicastWorkerExecutor-"));
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
		UnicastOutgoingConnection connection = null;
		DatagramSocket datagramSocket = null;
		lock.writeLock().lock();
		try {
			int nextConnectorIdx = getNextConnector();
			ConnectorHolder connectorHolder = this.connectors[nextConnectorIdx];
			datagramSocket = new DatagramSocket();
			connection = new UnicastOutgoingConnection(this, datagramSocket, address, connectorHolder.sender, nextConnectorIdx);
			this.connections.add(connection);
			connectorHolder.count++;
			this.connectionCount++;
		} catch (Exception e) {
			log.error("connect: {}", e.toString());
		} finally {
			lock.writeLock().unlock();
		}
		
		if (connection == null) {
			if (datagramSocket != null) {
				try {
					datagramSocket.close();
				} catch (Exception e) {
					log.error("close: {}", e.toString());
				}
			}
			datagramSocket = null;
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
		UnicastOutgoingConnection tempConnection = null;
		DatagramSocket datagramSocket = null;
		lock.writeLock().lock();
		try {
			if (connection instanceof UnicastOutgoingConnection && connections.contains(connection)) {
				tempConnection = (UnicastOutgoingConnection) connection;
				int idx = tempConnection.connectorIndex;
				ConnectorHolder localConnectorHolder = this.connectors[idx];
				localConnectorHolder.count -= 1;
				this.connectionCount -= 1L;
				datagramSocket = tempConnection.socket;
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
				if (datagramSocket != null) {
					try {
						datagramSocket.close();
					} catch (Exception e) {
						log.error("datagramSocket close: {}", e.toString());
					}
				}
				datagramSocket = null;
			}
		}
	}

	@Override
	public long getConnectionCount() {

		long count = 0;
		lock.readLock().lock();
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
