package com.sms.server.net.udp;

import java.net.SocketAddress;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * UDP Message Handler
 * @author pengliren
 *
 */
public interface IUDPMessageHandler {

	public void handleMessage(SocketAddress address, IoBuffer buffer);

	public void sessionOpened(IUDPTransportSession session);

	public void sessionClosed(IUDPTransportSession session);
}
