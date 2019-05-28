package com.sms.server.net.rtp.packetizer;

import gov.nist.javax.sdp.MediaDescriptionImpl;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.sdp.SdpFactory;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.flv.FLVUtils;
import com.sms.io.utils.BufferUtils;
import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.media.h264.H264CodecConfigInfo;
import com.sms.server.media.h264.H264CodecConfigParts;
import com.sms.server.media.h264.H264Utils;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.rtp.RTPPacket;
import com.sms.server.stream.codec.VideoCodec;

/**
 * RTP RFC 3984
 * @author pengliren
 *
 */
public class RTPPacketizerRFC3984H264 extends RTPPacketizerVideoBase implements IRTPPacketizer {

	 /**Single NALU Packet 
	  * 0                   1                   2                   3
	    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |F|NRI|  type   |                                               |
	   +-+-+-+-+-+-+-+-+                                               |
	   |                                                               |
	   |               Bytes 2..n of a Single NAL unit                 |
	   |                                                               |
	   |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                               :...OPTIONAL RTP padding        |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   */

	 /**Aggregation Packet
	  * 0                   1                   2                   3
	    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                          RTP Header                           |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |STAP-A NAL HDR |         NALU 1 Size           | NALU 1 HDR    |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                         NALU 1 Data                           |
	   :                                                               :
	   +               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |               | NALU 2 Size                   | NALU 2 HDR    |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                         NALU 2 Data                           |
	   :                                                               :
	   |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                               :...OPTIONAL RTP padding        |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	  *  
	  *  */

	 /** Fragmentation Units (FUs)
	  * 0                   1                   2                   3
	    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   | FU indicator  |   FU header   |                               |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+                               |
	   |                                                               |
	   |                         FU payload                            |
	   |                                                               |
	   |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                               :...OPTIONAL RTP padding        |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   
	  * The FU indicator octet has the following format:
	   +---------------+
	   |0|1|2|3|4|5|6|7|
	   +-+-+-+-+-+-+-+-+
	   |F|NRI|  Type   |
	   +---------------+
	   
	  * The FU header has the following format:
	   +---------------+
	   |0|1|2|3|4|5|6|7|
	   +-+-+-+-+-+-+-+-+
	   |S|E|R|  Type   |
	   +---------------+
	   */
	private static Logger log = LoggerFactory.getLogger(RTPPacketizerRFC3984H264.class);	
	
	private AtomicBoolean isFirstPacketKeyFrameCheck = new AtomicBoolean(true);
	
	public RTPPacketizerRFC3984H264() {
		
		this.sdpTypeId = RTPTYPE_RFC3984H264;
		this.timeScale = 90000;
	}
	
	@Override
	public void handleStreamPacket(IStreamPacket packet) {

		if(!(packet instanceof VideoData)) 
			return;
		
		VideoData videoData = (VideoData)packet;
		videoRTMP2RTPH264(videoData);
	}
	
	/**
	 * RTMP TO RTP H264 Packet
	 * @param videoData
	 * @return List<RTPPacket>
	 */
	private void videoRTMP2RTPH264(VideoData videoData){
		
		/**
		 * UB[4]frame type
		 * UB[4] codec id
		 * --------------------AVC Video packet
		 * UI8 avc pakcet type
		 * SI24 compositation time
		 * 
		 */		
		IoBuffer dataBuff = videoData.getData().asReadOnlyBuffer();
		int len = dataBuff.remaining();
		if(len < 2) return;
		byte first = dataBuff.get();
		byte second = dataBuff.get(); 
		this.rtcpSender.sendRTCP(this, videoData.getTimestamp());// send rtcp sr;
		boolean result = FLVUtils.getVideoCodec(first) == VideoCodec.AVC.getId() && second != 0;
		// we must send first paket is video keyframe
		if(isFirstPacketKeyFrameCheck.get()) {
			if(first == 0x17) {
				isFirstPacketKeyFrameCheck.set(false);
			} else {
				return;
			}
		}
		
		if(len > 9 && result) {
			byte[] ctsBytes = new byte[3];
			dataBuff.get(ctsBytes);
			int cts = BufferUtils.byteArrayToInt(ctsBytes, 0, 3);
			long ts = videoData.getTimestamp() + cts;	      
			ts = Math.round(ts * (timeScale / 1000));
	        dataBuff.position(5);
	        int start = 5;
	        int packetLen;
	        while (dataBuff.remaining() > 4) {
				packetLen = dataBuff.getInt();
				start += 4;
				if (packetLen <= 0){
					log.error("AVCPacketType {}", second);
					log.error("startLen: "+start+" packetLen: "+packetLen+" totalLen: "+len);					
					break;
				}
				else if(start + packetLen > len) {
					log.error("AVCPacketType {}", second);
					log.error("startLen: "+start+" packetLen: "+packetLen+" totalLen: "+len);
					packetLen = len - start;				
				}
				
				byte[] nalu = new byte[packetLen];
				dataBuff.get(nalu);
				videoRTMP2RTPH264FU(nalu, ts);
				start += packetLen;
	            if (start >= len) {
	            	break;	             
	            }
			}
		}
	}
	
	/**
	 * RTMP TO RTP H264 FU Packet
	 * @param nalu
	 * @param rtpPackets
	 */
	private void videoRTMP2RTPH264FU(byte[] nalu, long ts) {
		
		int len = nalu.length;
		IoBuffer naluBuff = IoBuffer.wrap(nalu);
		if (len <= maxPacketSize) {		
					
			if(len < 0) return;
			RTPPacket rtpPacket = new RTPPacket();
			rtpPacket.setPayload(nalu);
			rtpPacket.setChannel((byte) 0x00);		
			rtpPacket.setMarker(true);
			rtpPacket.setPadding(false);	
			rtpPacket.setExtensions(false);
			rtpPacket.setTimestamp(ts);
			rtpPacket.setSeqNumber(getNextSequence());
			rtpPacket.setSsrc(getSsrc());
			rtpPacket.setPayloadType(sdpTypeId);
			write(rtpPacket);
			return;
		}
		
		int totalLen = maxPacketSize - 2; // fix fu last packet not send
		int fuCount = (int) Math.round(Math.ceil((float) len / (float) totalLen));
		
		byte naluHeader = naluBuff.get();
		int naluType = naluHeader & 0x1F;
		len -= 1;
		RTPPacket rtpPacket;
		for (int i = 0; i < fuCount; i++) {
			rtpPacket = new RTPPacket();
			rtpPacket.setChannel((byte) 0x00);
			rtpPacket.setExtensions(false);
			rtpPacket.setMarker(false);
			rtpPacket.setPadding(false);
			rtpPacket.setTimestamp(ts);
			rtpPacket.setSeqNumber(getNextSequence());
			rtpPacket.setSsrc(getSsrc());
			rtpPacket.setPayloadType(sdpTypeId);
			int read = Math.min(len, totalLen);
			byte[] payload = new byte[read + 2];
			payload[0] = ((byte) ((naluHeader & 0xE0) + 28));//FU indicator
			if (i == 0) {//FU header
				payload[1] = (byte) ((1 << 7) | naluType);//S = 1；E = 0；R = 0				
			} else if (i == (fuCount - 1)) {
				payload[1] = (byte) ((1 << 6) | naluType);//S = 0；E = 1；R = 0
				rtpPacket.setMarker(true);
			} else {
				payload[1] = ((byte) naluType);//S = 0；E = 0；R = 0
			}
			naluBuff.get(payload, 2, read);
			len -= read;
			rtpPacket.setPayload(payload);
			write(rtpPacket);
		}		
	}

	@Override
	public MediaDescriptionImpl getDescribeInfo(IoBuffer config) throws Exception {

		/**
		 * a=rtpmap:97 H264/90000 
		 * a=fmtp:97 packetization-mode=1;profile-level-id=42C01E;sprop-parameter-sets=Z0LAHtkDxWhAAAADAEAAAAwDxYuS,aMuMsg==
		 * a=cliprect:0,0,160,240 
		 * a=framesize:97 240-160 
		 * a=framerate:24.0
		 */
		
		MediaDescriptionImpl describeInfo = (MediaDescriptionImpl)SdpFactory.getInstance().createMediaDescription("video", 0, 0, "RTP/AVP", new int[]{sdpTypeId});
		StringBuilder sb = new StringBuilder();
		sb.append("packetization-mode=1");
		String profile = null;
		H264CodecConfigParts configParts = H264Utils.breakApartAVCC(config.asReadOnlyBuffer());
		H264CodecConfigInfo configInfo = null;
		if (config != null) {
			configInfo = H264Utils.decodeAVCC(config.asReadOnlyBuffer());
		}
		profile = configParts.getProfileLevelIdStr();
		if(profile.length() > 0) {
			sb.append(";profile-level-id=").append(profile.toUpperCase());
		}
        if (configParts.getSpropParameterSetsStr().length() > 0) {
        	sb.append(";sprop-parameter-sets=").append(configParts.getSpropParameterSetsStr());
        }
		describeInfo.setAttribute("rtpmap", sdpTypeId + " H264/" + timeScale);
		describeInfo.setAttribute("fmtp", sdpTypeId + " " + sb.toString());
        if (configInfo != null) {
			describeInfo.setAttribute("cliprect", "0,0," + configInfo.height + "," + configInfo.width);
            describeInfo.setAttribute("framesize", sdpTypeId + " " + configInfo.displayWidth + "-" +configInfo.displayHeight);
			describeInfo.setAttribute("framerate", String.valueOf(configInfo.frameRate));
        }
        return describeInfo;
	}

}
