package com.sms.server.net.udp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multicast Listener Theard
 * @author pengliren
 *
 */
public class MulticastListener extends Thread {

	private static final Logger log = LoggerFactory.getLogger(MulticastListener.class);
	
	private boolean running = true;
	private boolean quit = false;
	private MulticastSocket multicastSocket;
	private UDPDatagramConfig datagramConfig;
	private IUDPMessageHandler handler;
	private String ipAddress ;
	private int port;
	private AtomicLong packetCount = new AtomicLong();
	private static volatile int threadCount = 0;
	
	public synchronized boolean isRunning() {
		return this.running;
	}
	
	public synchronized void unbind() {
		try {
			if (multicastSocket != null)
				multicastSocket.leaveGroup(InetAddress.getByName(ipAddress));
		} catch (Exception e) {
			log.error("unbind: {}", e.toString());
		} finally {
			this.quit = true;
		}
	}
	
	public synchronized void bind(UDPDatagramConfig config, IUDPMessageHandler handler, String ipAddress, int port) {
		try {
			this.datagramConfig = config;
			this.handler = handler;
			this.ipAddress = ipAddress;
			this.port = port;
		} catch (Exception e) {
			log.error("bind: {}", e.toString());
		}
	}
	
	private void init() {
		try {
			log.info("bind: " + this.ipAddress + "/"+ this.port);
			threadCount++;
			this.setName("MulticastIncomingThread-" + threadCount);
			InetAddress address = InetAddress.getByName(this.ipAddress);
			this.multicastSocket = null;
			if (datagramConfig.isMulticastBindToAddress() && multicastSocket == null) {
				try {
					multicastSocket = new MulticastSocket(port);
				} catch (Exception e) {
					log.error("multicastSocket: {}", e.toString());
				}
			}
			
			if (this.multicastSocket == null) {
				try {
					multicastSocket = new MulticastSocket(port);
				} catch (Exception e) {
					log.error("multicastSocket: {}", e.toString());
				}
			}
			multicastSocket.setTrafficClass(datagramConfig.getTrafficClass());
			multicastSocket.setReceiveBufferSize(datagramConfig.getReceiveBufferSize());
			multicastSocket.setReuseAddress(datagramConfig.isReuseAddress());
			multicastSocket.setSoTimeout(datagramConfig.getMulticastTimeout());
			multicastSocket.joinGroup(address);
		} catch (Exception e) {
			log.error("init: {}", e.toString());
		}
	}
	
	@Override
	public void run() {
	
		init();
		int maxPacketSize = datagramConfig.getDatagramMaximumPacketSize();
		byte[] receiveByte;
		DatagramPacket dataPacket;
		IoBuffer data;
		while (true) {

			if (multicastSocket == null) {
				synchronized (this) {
					running = false;
				}
				break;
			}

			try {
				receiveByte = new byte[maxPacketSize];
				dataPacket = new DatagramPacket(receiveByte, maxPacketSize);
				multicastSocket.receive(dataPacket);
				int len = dataPacket.getLength();
				if (len > 0) {
					data = IoBuffer.wrap(receiveByte, 0, len);
					handler.handleMessage(dataPacket.getSocketAddress(), data);
					packetCount.incrementAndGet();
				}

				synchronized (this) {
					if (this.quit) {
						this.running = false;
						break;
					}
				}
			} catch (Exception e) {
				synchronized (this) {
					if (this.quit) {
						this.running = false;
						break;
					}
				}
			}
		}
		
		try {
			if (this.multicastSocket != null) 
				this.multicastSocket.close();
		} catch (Exception e) {
		}
	    log.info("run stop");
	}
}
