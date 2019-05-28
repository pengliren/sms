package com.sms.server.net.rtp.packetizer;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.net.rtp.RTPPacket;
import com.sms.server.net.rtp.RTPSSRCGeneratorSingleton;
import com.sms.server.net.rtsp.RTSPMinaConnection;
import com.sms.server.net.udp.IUDPTransportOutgoingConnection;

/**
 * RTP Packetizer Base
 * @author pengliren
 *
 */
public abstract class RTPPacketizerBase implements IRTPPacketizer {

	private static Logger log = LoggerFactory.getLogger(RTPPacketizerBase.class);
	
	protected AtomicInteger sequence = new AtomicInteger(0);

	protected IRTPPacketizerRTCPSender rtcpSender;

	protected AtomicLong packetCount = new AtomicLong();

	protected AtomicLong byteCount = new AtomicLong();

	protected boolean isRTPWrapped = false;

	protected String baseType = "ukn";

	protected int sdpTypeId;

	protected int maxPacketSize = 1440;

	protected int timeScale;

	private long ssrc;
	
	private boolean isUdp = false;
	
	private Object conn;

	public RTPPacketizerBase() {
		rtcpSender = new RTPPacketizerRTCPSender();
		ssrc = RTPSSRCGeneratorSingleton.getInstance().getNextSSRC();
	}
	
	@Override
	public void initRtcpInfo(InetSocketAddress address) throws SocketException {
		rtcpSender.init(address);
	}
	
	@Override
	public void stop() {
		
		if(conn instanceof IUDPTransportOutgoingConnection) {
			((IUDPTransportOutgoingConnection)conn).close();
		} else if(conn instanceof RTSPMinaConnection) {
			((RTSPMinaConnection)conn).close();
		} else {
			log.error("rtsp conn is null ?");
		}
		rtcpSender.close();
	}
	
	@Override
	public InetSocketAddress getRtcpAddress() {
		
		return rtcpSender.getAddress();
	}

	@Override
	public long getPacketCount() {

		return packetCount.get();
	}

	@Override
	public long getByteCount() {

		return byteCount.get();
	}

	@Override
	public long getRTCPOctetCount() {

		return byteCount.get() - packetCount.get() * 12;
	}

	@Override
	public void resetSequence() {

		sequence.set(0);
	}

	@Override
	public int getNextSequence() {

		return sequence.incrementAndGet();
	}

	@Override
	public int getSDPTypeId() {

		return sdpTypeId;
	}

	@Override
	public void setSDPTypeId(int sdpTypeId) {

		this.sdpTypeId = sdpTypeId;
	}

	@Override
	public long getSsrc() {

		return ssrc;
	}

	@Override
	public int getTimescale() {

		return timeScale;
	}
	
	@Override
	public void setOutputStream(IUDPTransportOutgoingConnection conn) {
		isUdp = true;
		this.conn = conn;
	}
	
	@Override
	public void setOutputStream(RTSPMinaConnection conn) {
		isUdp = false;
		this.conn = conn;
	}
	
	@Override
	public void write(RTPPacket packet) {	
		if(isUdp) {
			byte[] data = packet.toBytes();
			((IUDPTransportOutgoingConnection)conn).sendMessage(data, 0, data.length);
		} else {
			((RTSPMinaConnection)conn).write(packet);
		}
		packetCount.incrementAndGet();
        byteCount.addAndGet(packet.getPayload().length);
	}	
}
