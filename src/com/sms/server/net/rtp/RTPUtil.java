package com.sms.server.net.rtp;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.utils.HexDump;

/**
 * RTP Util
 * @author pengliren
 *
 */
public class RTPUtil {

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
	
	
	private static Logger log = LoggerFactory.getLogger(RTPUtil.class);
	
	public final static int MAX_PACKET_SIZE = 1440;
	public static final String H264_SP_SDP = "sprop-parameter-sets=";
	public static final String AAC_SP_SDP = "config=";
	
	/**
	 * rtp video sdp format to rtmp
	 * @param rtpmap
	 * @param fmtp
	 * @return
	 * @throws Exception
	 */
	public static IoBuffer decodeVideoConfigure(String rtpmap, String fmtp) throws Exception {
		
		// support h264
		if(StringUtils.containsIgnoreCase(rtpmap, "h264")) {
			return SDP2RTMPH264(fmtp);
		}
		
		// TODO if you need support more format...
		return null;
	}
	
	/**
	 * rtp audio sdp format to rtmp
	 * @param rtpmap
	 * @param fmtp
	 * @return
	 * @throws Exception
	 */
	public static IoBuffer decodeAudioConfigure(String rtpmap, String fmtp) {
				
		// support aac
		if(StringUtils.containsIgnoreCase(rtpmap, "mpeg4-generic")) {
			return SDP2RTMPAAC(fmtp);
		}
		
		// TODO if you need support more format...
		return null;
	}
	
	/**
	 * sdp to rtmp for h264
	 * @param configure
	 * @return
	 * @throws Exception
	 */
	public static IoBuffer SDP2RTMPH264(String configure) throws Exception {
		
		// 96 profile-level-id=42C0E1; sprop-parameter-sets=Z0LAHqtAWgi0IAAAAwAgAAADA9HixdQ=,aM4yyA==; packetization-mode=1
		int idx = configure.lastIndexOf(H264_SP_SDP); // spss start index
		int ldx = configure.lastIndexOf(";"); // spss last index
		// fix ldx pos
		if(ldx < idx || ldx == 0) ldx = configure.length() - 1;		
		String sps;
		String[] ppss;
		if(idx != -1) {
			String temp = configure.substring(idx + H264_SP_SDP.length(), ldx);
			String[] spsppss = temp.split(",");
			sps = spsppss[0];
			ppss = ArrayUtils.remove(spsppss, 0);
			byte[] spsBuff = Base64.decodeBase64(sps.getBytes());
			int confLen = 2 + 3 + 5 + 1 + 2 + spsBuff.length + 3 + 10;
			IoBuffer config = IoBuffer.allocate(confLen).setAutoExpand(true);
			config.put((byte) 0x17);
			config.put((byte) 0x00);
			config.put((byte) 0x00);
			config.put((byte) 0x00);
			config.put((byte) 0x00);
			config.put((byte) 0x01);
			config.put((byte) spsBuff[1]);
			config.put((byte) spsBuff[2]);
			config.put((byte) spsBuff[3]);
			config.put((byte) 0xFF);
			config.put((byte) 0xE1);
			config.putShort((short) spsBuff.length);
			config.put(spsBuff);
			// pps size
			config.put((byte) ppss.length);
			byte[] ppsBuff;
			for(String pps : ppss) {				
				ppsBuff = Base64.decodeBase64(pps.getBytes());
				config.putShort((short) ppsBuff.length);
				config.put(ppsBuff);
			}
			config.flip();
			return config;
		}
		return null;
	}
	
	/**
	 * decode aac sdp to rtmp config
	 * @param configure
	 * @return
	 */
	public static IoBuffer SDP2RTMPAAC(String configure) {
		
		int idx = configure.lastIndexOf(AAC_SP_SDP); 
		if(idx != -1) {
			String aac = configure.substring(idx + AAC_SP_SDP.length());
			byte[] aacBuff = HexDump.hexStringToByteArray(aac);
			IoBuffer aacConfigure = IoBuffer.allocate(2 + aacBuff.length);
			aacConfigure.put((byte)0xAF);
			aacConfigure.put((byte)0x00);
			aacConfigure.put(aacBuff);
			aacConfigure.flip();
			return aacConfigure;
		}
		return null;		
	}
	
	/**
	 * decode rtsp range
	 * @param range (Range: npt=120.875-)
	 * @return
	 */
	public static double[] decodeRangeHeader(String range) {
		
		double[] rangeNtps = new double[2];
		rangeNtps[0] = -1.0D;
		rangeNtps[1] = -1.0D;
		if (!StringUtils.isEmpty(range)) {
			if (range.startsWith("npt")) {
				String ntpValues = range.substring(4).trim();
				int idx = ntpValues.indexOf("-");
				String startNtp = null;
				String endNtp = null;
				if (idx >= 0) {
					endNtp = ntpValues.substring(idx + 1).trim();
					startNtp = ntpValues.substring(0, idx).trim();
					if (endNtp.length() <= 0)
						endNtp = null;
				}

				// vod stream
				if (!startNtp.startsWith("now")) {
					rangeNtps[0] = Double.parseDouble(startNtp);
					if (endNtp != null) rangeNtps[1] = Double.parseDouble(endNtp);
				}
			}
		} else {
			log.info("RTPUtils.decodeRangeHeader: Range format not supported: {}",range);
		}
	     
	    return rangeNtps;
	}
	
	//96 H264/90000
	public static int decodeRtpmapVideoTimescale(String rtpmap) {
		
		int timescale = -1;
		int idx = rtpmap.lastIndexOf("/");
		if(idx != -1) {
			timescale = Integer.parseInt(rtpmap.substring(idx + 1));
		}
		return timescale; 
	}
	
	//97 MPA/48000/2
	public static int decodeRtpmapAudioTimescale(String rtpmap) {
		
		int timescale = -1;
		String[] segments = rtpmap.split("/");
		if(segments.length == 3) {
			timescale = Integer.parseInt(segments[1]);
		}
		return timescale; 
	}
}
