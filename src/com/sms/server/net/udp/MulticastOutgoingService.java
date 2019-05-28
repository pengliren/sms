package com.sms.server.net.udp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.Configuration;
import com.sms.server.api.stream.IBroadcastStream;
import com.sms.server.api.stream.IStreamListener;
import com.sms.server.api.stream.IStreamPacket;

/**
 * Multicast Out Stream Service 
 * @author pengliren
 *
 */
public class MulticastOutgoingService implements IStreamListener {

	private static Logger log = LoggerFactory.getLogger(MulticastOutgoingService.class);
	
	private ConcurrentMap<String, IUDPPacketizer> packetizerMap = new ConcurrentHashMap<String, IUDPPacketizer>();
	
	private MulticastOutgoing multicastOutgoing;

	private static final class SingletonHolder {

		private static final MulticastOutgoingService INSTANCE = new MulticastOutgoingService();
	}
	
	public static MulticastOutgoingService getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
	private MulticastOutgoingService() {
		
		multicastOutgoing = new MulticastOutgoing();
		multicastOutgoing.init(Configuration.MULTICAST_EXECUTOR_THREADS);
	}
	
	public void register(IBroadcastStream stream, UDPDatagramConfig config, String host, int port) {
		
		unregister(stream.getPublishedName());// check if already have the stream, we must stop stream  
		IUDPTransportOutgoingConnection conn = multicastOutgoing.connect(config, host, port);
		IUDPPacketizer udpPacketizer = new UDPPacketizerMPEGTS(conn);
		packetizerMap.put(stream.getPublishedName(), udpPacketizer);
		log.info("add multicast outgoing stream : {} bind : {}", stream.getPublishedName(), String.format("udp://%s:%d", host, port));
	}
	
	public void unregister(String name) {
		
		IUDPPacketizer udpPacketizer = packetizerMap.remove(name);
		if(udpPacketizer != null) {
			udpPacketizer.stop();
			log.info("remove multicast outgoing stream : {} unbind: {}", name, udpPacketizer.getConnection().getAddress().toString());
		}
	}
	
	@Override
	public void packetReceived(IBroadcastStream stream, IStreamPacket packet) {
		
		String name = stream.getPublishedName();
		IUDPPacketizer udpPacketizer = packetizerMap.get(name);
		udpPacketizer.handleStreamPacket(packet);
	}
	
	public IUDPPacketizer getUDPPacketizer(String name) {

		return packetizerMap.get(name);
	}
}
