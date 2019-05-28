package com.sms.server.net.udp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * UDP Sender Thread
 * @author pengliren
 *
 */
public class UDPSender implements Runnable {

	private AtomicLong packetCount = new AtomicLong(0);
	private boolean running = false;
	private List<UDPMessage> messages = new ArrayList<UDPSender.UDPMessage>();
	private Executor executor;

	public UDPSender(Executor executor) {
		this.executor = executor;
	}
	
	public long incPacketCount() {
		return packetCount.incrementAndGet();
	}
	 
	public synchronized void handleMessage(IUDPSender sender, byte[] data, int pos, int len) {
		this.messages.add(new UDPMessage(sender, data, pos, len));
		if (!this.running) {
			this.running = true;
			this.executor.execute(this);
		}
	}
	
	public Executor getExecutor() {
		return this.executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void run() {
		while (true) {
			UDPMessage message = null;
			synchronized (this) {
				if (this.messages.size() > 0) {
					message = (UDPMessage) this.messages.remove(0);
				} else {
					this.running = false;
					break;
				}
			}
			if (message != null)
				message.handler.handleSendMessage(message.buffer, message.offset, message.len);
		}
	}
	
	class UDPMessage {
		public IUDPSender handler = null;
		public byte[] buffer = null;
		public int offset = 0;
		public int len = 0;

		public UDPMessage(IUDPSender sender, byte[] data, int pos, int len) {
			this.handler = sender;
			this.buffer = data;
			this.offset = pos;
			this.len = len;
		}
	}
}
