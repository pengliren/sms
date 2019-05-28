package com.sms.server.net.rtp.packetizer;

import gov.nist.javax.sdp.MediaDescriptionImpl;

import javax.sdp.SdpFactory;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.io.flv.FLVUtils;
import com.sms.io.mp3.impl.MP3Header;
import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtp.RTPPacket;
import com.sms.server.stream.codec.AudioCodec;

/**
 * RTP RFC2250MP3
 * @author pengliren
 *
 */
public class RTPPacketizerRFC2250MP3 extends RTPPacketizerAudioBase implements IRTPPacketizer {

	public RTPPacketizerRFC2250MP3() {
		this.sdpTypeId = RTPTYPE_MP3;
		this.timeScale = 90000;
	}
	
	@Override
	public void handleStreamPacket(IStreamPacket packet) {

		if (!(packet instanceof AudioData))
			return;

		AudioData audioData = (AudioData) packet;
		this.rtcpSender.sendRTCP(this, audioData.getTimestamp());// send rtcp sr;
		long ts = Math.round(audioData.getTimestamp() * (timeScale / 1000));
		IoBuffer dataBuff = audioData.getData().asReadOnlyBuffer();
		if(dataBuff.remaining() < 2) return;
		byte first = dataBuff.get();
		boolean result = FLVUtils.getAudioCodec(first) == AudioCodec.MP3.getId();
		if(result == false) return;
		RTPPacket rtpPacket = new RTPPacket();
		rtpPacket.setChannel((byte) 0x02);
		rtpPacket.setExtensions(false);
		rtpPacket.setMarker(true);
		rtpPacket.setPadding(false);
		rtpPacket.setTimestamp(ts);
		rtpPacket.setSeqNumber(getNextSequence());
		rtpPacket.setSsrc(getSsrc());
		rtpPacket.setPayloadType(sdpTypeId);
		
		int ausize = dataBuff.remaining();
		byte[] payload = new byte[ausize + 4];
		payload[0] = 0;//mp2,3 start code
		payload[1] = 0;
		payload[2] = 0;
		payload[3] = 0;		
		dataBuff.get(payload, 4, ausize);		
		rtpPacket.setPayload(payload);
		write(rtpPacket);
	}
	
	@Override
	public MediaDescriptionImpl getDescribeInfo(IoBuffer config) throws Exception {
				
		if(config.remaining() < 4) return null;
		config.position(1);
		MP3Header header = new MP3Header(config.getInt());
		int channels = 0;
		switch (header.getChannelMode()) {
		case 0:
			channels = 2;
			break;
		case 1:
			channels = 2;
			break;
		case 2:
			channels = 2;
			break;
		case 3:
			channels = 1;
			break;
		}
		MediaDescriptionImpl describeInfo;//audio md
		describeInfo = (MediaDescriptionImpl)SdpFactory.getInstance().createMediaDescription("audio", 0, 0, "RTP/AVP", new int[]{sdpTypeId});
		describeInfo.setAttribute("rtpmap",
				new StringBuilder()
						.append(sdpTypeId)
						.append(" MPA/")
						.append(timeScale)
						.append("/")
						.append(channels)
						.toString());
        return describeInfo;
	}
}
