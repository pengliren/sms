package com.sms.server.net.rtp.packetizer;

import static com.sms.io.ts.TransportStreamUtils.TIME_SCALE;
import gov.nist.javax.sdp.MediaDescriptionImpl;

import java.util.concurrent.atomic.AtomicInteger;

import javax.sdp.SdpFactory;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.ts.FLV2MPEGTSWriter;
import com.sms.io.ts.IFLV2MPEGTSWriter;
import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.rtp.RTPPacket;
import com.sms.server.util.SystemTimer;
/**
 * RTP Packetizer MPEGTS
 * @author pengliren
 * 
 */
public class RTPPacketizerMPEGTS extends RTPPacketizerVideoBase implements IRTPPacketizer, IFLV2MPEGTSWriter {

	private static Logger log = LoggerFactory.getLogger(RTPPacketizerMPEGTS.class);
	
	private FLV2MPEGTSWriter flv2tsWriter;
	
	protected long lastPAT = -1L;
	
	protected int mpegtsPacketsPerBlock = 7;
	
	private IoBuffer buffer;
	
	private AtomicInteger pcount = new AtomicInteger(0);
	  
	public RTPPacketizerMPEGTS(IoBuffer videoConfig, IoBuffer audioConfig) {
	
		this.sdpTypeId = RTPTYPE_MP2T;
		this.timeScale = 90000;
		flv2tsWriter = new FLV2MPEGTSWriter(this, videoConfig, audioConfig);
		log.info("flv to mepgts rtp stream!");
	}
		
	@Override
	public void handleStreamPacket(IStreamPacket packet) {

		long cts = SystemTimer.currentTimeMillis();
		long ts = packet.getTimestamp() * TIME_SCALE;
		if ((lastPAT == -1L) || (cts - lastPAT > 100L)) {
			lastPAT = cts;
			flv2tsWriter.addPAT(ts);
		}
		
		if (packet instanceof VideoData) { // handle video
			flv2tsWriter.handleVideo((VideoData) packet);
		} else if (packet instanceof AudioData) {// handle audio
			flv2tsWriter.handleAudio((AudioData) packet);
		}
	}
	
	/**
	 * callback next ts packet
	 */
	@Override
	public void nextBlock(long ts, byte[] block) {
		
		if(buffer == null) {
			buffer = IoBuffer.allocate(1500).setAutoExpand(true);
		}
		
		if(pcount.get() >= mpegtsPacketsPerBlock) {
			buffer.flip();
			flushBlock(ts);
			pcount.set(0);
			buffer.position(0);
		}
		buffer.put(block);
		pcount.incrementAndGet();
	}
	
	/**
	 * flush ts block
	 * @param ts
	 */
	private void flushBlock(long ts) {
		
		byte[] payload = new byte[buffer.remaining()];
		buffer.get(payload);
		
		RTPPacket rtpPacket = new RTPPacket();
		rtpPacket.setPayload(payload);
		rtpPacket.setChannel((byte) 0x00);		
		rtpPacket.setMarker(false);
		rtpPacket.setPadding(false);	
		rtpPacket.setExtensions(false);
		rtpPacket.setTimestamp(ts);
		rtpPacket.setSeqNumber(getNextSequence());
		rtpPacket.setSsrc(getSsrc());
		rtpPacket.setPayloadType(sdpTypeId);
		write(rtpPacket);
	}
	
	@Override
	public MediaDescriptionImpl getDescribeInfo(IoBuffer config) throws Exception {

		MediaDescriptionImpl describeInfo;//audio md
		describeInfo = (MediaDescriptionImpl)SdpFactory.getInstance().createMediaDescription("video", 0, 0, "RTP/AVP", new int[]{sdpTypeId});
		describeInfo.setAttribute("rtpmap",
				new StringBuilder()
						.append(sdpTypeId)
						.append(" MP2T/")
						.append(timeScale)
						.toString());
        return describeInfo;
	}
}
