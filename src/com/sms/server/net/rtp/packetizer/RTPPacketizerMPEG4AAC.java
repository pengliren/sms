package com.sms.server.net.rtp.packetizer;

import gov.nist.javax.sdp.MediaDescriptionImpl;

import javax.sdp.SdpFactory;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.flv.FLVUtils;
import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.media.aac.AACFrame;
import com.sms.server.media.aac.AACUtils;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtp.RTPPacket;
import com.sms.server.stream.codec.AudioCodec;

/**
 * RTP Packetizer MPEG4AAC
 * @author pengliren
 *
 */
public class RTPPacketizerMPEG4AAC extends RTPPacketizerAudioBase implements IRTPPacketizer{

	private static Logger log = LoggerFactory.getLogger(RTPPacketizerMPEG4AAC.class);
	
	public RTPPacketizerMPEG4AAC() {
		
		this.sdpTypeId = RTPTYPE_MPEG4AAC;
		this.timeScale = 12000;
	}
	
	@Override
	public void handleStreamPacket(IStreamPacket packet) {

		if (!(packet instanceof AudioData))
			return;
		
		AudioData audioData = (AudioData) packet;
		this.rtcpSender.sendRTCP(this, audioData.getTimestamp());// send rtcp sr;
		long ts = Math.round(audioData.getTimestamp() * (timeScale / 1000));
		IoBuffer dataBuff = audioData.getData().asReadOnlyBuffer();

		if(dataBuff.remaining() < 4) {
			log.info("data remaining ?");
			return;
		}
		byte first = dataBuff.get();
		byte second = dataBuff.get(); 
		boolean result = FLVUtils.getAudioCodec(first) == AudioCodec.AAC.getId() && second != 0;
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
		payload[0] = (byte) 0x00;
		payload[1] = (byte) 0x10;
		/* Write the AU header size */
		byte[] auheader = new byte[2];
		//au-size 13bit
		auheader[0] = (byte) ((ausize & 0x1FE0) >> 5);
		//au-index 3bit
		auheader[1] = (byte) ((ausize & 0x1F) << 3);
		payload[2] = auheader[0];
		payload[3] = auheader[1];
		dataBuff.get(payload, 4, ausize);
		rtpPacket.setPayload(payload);
		
		write(rtpPacket);		
	}

	@Override
	public MediaDescriptionImpl getDescribeInfo(IoBuffer config) throws Exception {
	
		MediaDescriptionImpl describeInfo;//audio md
		/**
		 * a=rtpmap:96 mpeg4-generic/12000/2 
		 * a=fmtp:96 profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=1490
		 */
		describeInfo = (MediaDescriptionImpl)SdpFactory.getInstance().createMediaDescription("audio", 0, 0, "RTP/AVP", new int[]{sdpTypeId});
		AACFrame aacFrame = AACUtils.decodeAACCodecConfig(config);
		describeInfo.setAttribute("rtpmap", 
						new StringBuilder()
								.append(sdpTypeId)
								.append(" mpeg4-generic/")
								.append(timeScale).append("/")
								.append(aacFrame.getChannels())
								.toString());
		config.position(2);
		describeInfo.setAttribute("fmtp",
						new StringBuilder()
								.append(sdpTypeId)
								.append(" profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=")
								.append(config.getHexDump().replace(" ", ""))
								.toString());
        return describeInfo;
	}

}
