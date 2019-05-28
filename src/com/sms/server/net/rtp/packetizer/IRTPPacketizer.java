package com.sms.server.net.rtp.packetizer;

import gov.nist.javax.sdp.MediaDescriptionImpl;

import java.net.InetSocketAddress;
import java.net.SocketException;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.net.rtp.RTPPacket;
import com.sms.server.net.rtsp.RTSPMinaConnection;
import com.sms.server.net.udp.IUDPTransportOutgoingConnection;

/**
 * RTP Packetizer Interface
 * @author pengliren
 *
 */
public interface IRTPPacketizer {

	public static final int RTPTYPE_RFC3984H264 = 97;
	public static final int RTPTYPE_MPEG4AAC = 96;
	public static final int RTPTYPE_MPEG4LATM = 96;
	public static final int RTPTYPE_RFC2190H263 = 96;
	public static final int RTPTYPE_SPEEX = 97;
	public static final int RTPTYPE_MP3 = 14;
	public static final int RTPTYPE_MP2T = 98;

	public void handleStreamPacket(IStreamPacket packet);
	
	public void initRtcpInfo(InetSocketAddress address) throws SocketException;

	public long getPacketCount();

	public long getByteCount();

	public long getRTCPOctetCount();

	public MediaDescriptionImpl getDescribeInfo(IoBuffer config)throws Exception;

	public int getSDPTypeId();

	public void setSDPTypeId(int sdpTypeId);

	public void resetSequence();

	public int getNextSequence();
	
	public long getSsrc();
	
	public int getTimescale();
	
	public void stop();
	
	public InetSocketAddress getRtcpAddress();
	
	public void write(RTPPacket packet);
	
	// tcp
	public void setOutputStream(RTSPMinaConnection conn);
	
	// udp
	public void setOutputStream(IUDPTransportOutgoingConnection conn);
}