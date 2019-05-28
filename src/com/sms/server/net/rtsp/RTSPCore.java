package com.sms.server.net.rtsp;

import static com.sms.server.net.http.message.HTTPHeaders.Names.CONTENT_LENGTH;
import static com.sms.server.net.http.message.HTTPHeaders.Names.CONTENT_TYPE;
import static com.sms.server.net.http.message.HTTPHeaders.Names.RANGE;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.CACHE_CONTROL;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.CSEQ;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.SERVER;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.SESSION;
import static com.sms.server.net.rtsp.message.RTSPMethods.ANNOUNCE;
import static com.sms.server.net.rtsp.message.RTSPMethods.DESCRIBE;
import static com.sms.server.net.rtsp.message.RTSPMethods.GET_PARAMETER;
import static com.sms.server.net.rtsp.message.RTSPMethods.OPTIONS;
import static com.sms.server.net.rtsp.message.RTSPMethods.PAUSE;
import static com.sms.server.net.rtsp.message.RTSPMethods.PLAY;
import static com.sms.server.net.rtsp.message.RTSPMethods.RECORD;
import static com.sms.server.net.rtsp.message.RTSPMethods.REDIRECT;
import static com.sms.server.net.rtsp.message.RTSPMethods.SETUP;
import static com.sms.server.net.rtsp.message.RTSPMethods.SET_PARAMETER;
import static com.sms.server.net.rtsp.message.RTSPMethods.TEARDOWN;
import gov.nist.javax.sdp.MediaDescriptionImpl;
import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sdp.Attribute;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.flv.FLVUtils;
import com.sms.server.Scope;
import com.sms.server.api.SMS;
import com.sms.server.api.ScopeUtils;
import com.sms.server.api.stream.IStreamPlaybackSecurity;
import com.sms.server.api.stream.IStreamSecurityService;
import com.sms.server.api.stream.support.SimplePlayItem;
import com.sms.server.net.http.codec.HTTPCodecUtil;
import com.sms.server.net.http.codec.QueryStringDecoder;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.http.stream.CustomSingleItemSubStream;
import com.sms.server.net.rtp.RTPPlayer;
import com.sms.server.net.rtp.RTPUtil;
import com.sms.server.net.rtp.packetizer.IRTPPacketizer;
import com.sms.server.net.rtp.packetizer.RTPPacketizerMPEG4AAC;
import com.sms.server.net.rtp.packetizer.RTPPacketizerMPEGTS;
import com.sms.server.net.rtp.packetizer.RTPPacketizerRFC2250MP3;
import com.sms.server.net.rtp.packetizer.RTPPacketizerRFC3984H264;
import com.sms.server.net.rtsp.message.RTSPHeaders;
import com.sms.server.net.rtsp.message.RTSPResponseStatuses;
import com.sms.server.net.udp.UDPPortManager;
import com.sms.server.stream.proxy.RTSPPushProxyStream;

/**
 * rtsp protocol process
 * @author pengliren
 *
 */
public final class RTSPCore {
	
	private static Logger log = LoggerFactory.getLogger(RTSPCore.class);
	
	private static Pattern rtspTransportPattern = Pattern.compile(".*client_port=(\\d*)-(\\d*).*");

	public static ConcurrentHashMap<String, RTPPlayer> rtpSocketMaps = new ConcurrentHashMap<String, RTPPlayer>();
	
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public static boolean handleRtspMethod(HTTPRequest request, HTTPResponse response) throws Exception {
		
		boolean flag = true;
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		response.setHeader(SESSION, String.format("%s;%s", conn.getSessionId(),"timeout=60"));
		response.setHeader(SERVER, "SMS");
		response.setHeader(CSEQ, String.valueOf(request.getHeader("CSeq")));
		response.setHeader(CACHE_CONTROL, "no-cache");
		if(request.getMethod() == OPTIONS){
			RTSPCore.options(request, response);
		} else if(request.getMethod() == DESCRIBE) {
			RTSPCore.describe(request, response);
		} else if(request.getMethod() == ANNOUNCE) {
			RTSPCore.announce(request, response);
		} else if(request.getMethod() == SETUP) {
			RTSPCore.setup(request, response);
		} else if(request.getMethod() == PLAY) {
			RTSPCore.play(request, response);
		} else if(request.getMethod() == PAUSE) {
			RTSPCore.pause(request, response);
		} else if(request.getMethod() == TEARDOWN) {
			RTSPCore.teardown(request, response);
		} else if(request.getMethod() == GET_PARAMETER) {
			RTSPCore.getParameter(request, response);
		} else if(request.getMethod() == SET_PARAMETER) {
			RTSPCore.setParameter(request, response);
		} else if(request.getMethod() == REDIRECT) {
			RTSPCore.redirect(request, response);
		} else if(request.getMethod() == RECORD) {
			RTSPCore.record(request, response);
		} else {
			flag = false;
		}
		
		return flag;
	}
	
	/**
	 * rtsp options method
	 * @param request
	 * @param response
	 */
	public static void options(HTTPRequest request, HTTPResponse response) {

		response.setHeader("Public", "DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE, OPTIONS, ANNOUNCE, RECORD, GET_PARAMETER");
		response.setHeader("Supported", "play.basic, con.persistent");
	}
	
	/**
	 * rtsp describe method
	 * @param request
	 * @param response
	 * @param session
	 */
	public static void describe(HTTPRequest request, HTTPResponse response) {
		
		try {	
			RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
			RTSPConnectionConsumer rtspConsumer = createPlayStream(request.getUri());
			if (rtspConsumer == null) {
				response.setStatus(RTSPResponseStatuses.NOT_FOUND);
				return;
			}			
			conn.setAttribute("rtspConsumer", rtspConsumer);			
			response.addHeader(CONTENT_TYPE, "application/sdp");
			String sdp = configureSDP();
			response.addHeader(CONTENT_LENGTH, sdp.length());
			response.setContent(HTTPCodecUtil.encodeBody(sdp));
		} catch (Exception e) {	
			log.info("describe exception {}", e.toString());
			response.setStatus(RTSPResponseStatuses.BAD_REQUEST);
		}			
	}
	
	/**
	 * rtsp method announce
	 * @param request
	 * @param response
	 * @param session
	 * @throws Exception
	 */
	public static void announce(HTTPRequest request, HTTPResponse response) throws Exception {
		
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		// make sure we only handle application/sdp
		String contentType = request.getHeader(RTSPHeaders.Names.CONTENT_TYPE);
		if(contentType == null || !contentType.equalsIgnoreCase("application/sdp")) {
			response.setStatus(RTSPResponseStatuses.BAD_REQUEST);
			return;
		}
		// get the sdp
		String sdp = HTTPCodecUtil.decodeBody(request.getContent());
		// get the first video track and audio track
		if(sdp != null) {
			SDPAnnounceParser parser = new SDPAnnounceParser (sdp);
			SessionDescriptionImpl sessiondescription = parser.parse();
			
			RTSPPushProxyStream stream = createPublishStream(request.getUri());
			conn.setAttribute("pushStream", stream);
			conn.setAttribute("sdp", sessiondescription);
			conn.setAttribute("isInbound", true);
		} else {
			response.setStatus(RTSPResponseStatuses.BAD_REQUEST);
		}
	}
	
	/**
	 * rtsp method getParameter
	 * @param request
	 * @param response
	 */
	public static void getParameter(HTTPRequest request, HTTPResponse response) {
		
		//System.out.println("getParameter: "+request);
	}
	
	/**
	 * rtsp method setParameter
	 * @param request
	 * @param response
	 */
	public static void setParameter(HTTPRequest request, HTTPResponse response) {
		
		//System.out.println("setParameter: "+request);
	}
	
	/**
	 * rtsp method pause
	 * @param request
	 * @param response
	 * @param session
	 */
	public static void pause(HTTPRequest request, HTTPResponse response) {
	
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		CustomSingleItemSubStream rtspStream = (CustomSingleItemSubStream)conn.getAttribute("rtspStream");
		if(rtspStream != null) {
			rtspStream.pause(0);
		}
		log.info("stream pause {}", request.getUri());
	}
	
	/**
	 * rtsp method play
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException
	 */
	public static void play(HTTPRequest request, HTTPResponse response) throws Exception {
		
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		final RTSPConnectionConsumer rtspConsumer = (RTSPConnectionConsumer)conn.getAttribute("rtspConsumer");
		if (rtspConsumer == null) {
			response.setStatus(RTSPResponseStatuses.NOT_FOUND);
			return;
		}
		
		CustomSingleItemSubStream rtspStream = (CustomSingleItemSubStream)conn.getAttribute("rtspStream");
		double[] rangeNtp = RTPUtil.decodeRangeHeader(request.getHeader(RANGE));
		if(rangeNtp[0] > 0) {
			log.info("rtsp seekTo {}", rangeNtp[0]);
			rtspStream.seek(Math.round((float)(rangeNtp[0] * 1000)));
		}
		//RTP-Info: url=rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov/trackID=1;seq=1;rtptime=0,url=rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov/trackID=2;seq=1;rtptime=0
		log.info("rtsp play {} by type {}", request.getUri(), (conn.getPlayType() == 0 ? "udp" : "tcp"));
		if(conn.hasAttribute("playing")) {
			int pos = 0;
			if(!conn.isLive()) pos = Math.round((float)(rangeNtp[0] * 1000));  
			rtspStream.resume(pos);
			return;
		}		
		// first play		
		RTPPlayer player = conn.getRtpConnector();
		if (player != null) {
			rtspConsumer.addStreamListener(player);
		}
		conn.setAttribute("playing", true);
	}
	
	/**
	 * rtsp record method
	 * @param request
	 * @param response
	 */
	public static void record(HTTPRequest request, HTTPResponse response) {
		
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		RTSPPushProxyStream pushStream = (RTSPPushProxyStream)conn.getAttribute("pushStream");
		if(pushStream != null) {
			pushStream.start();
			
			IoBuffer videoConfig = (IoBuffer)conn.getAttribute("videoConfig");
			if(videoConfig != null) {
				pushStream.setAVCConfig(videoConfig);
				pushStream.setVideoTimescale((Integer)conn.getAttribute("rtpmapVideoTimescale"));
			}
			
			IoBuffer audioConfig = (IoBuffer)conn.getAttribute("audioConfig");
			if(audioConfig != null) {
				pushStream.setAACConfig(audioConfig);
				pushStream.setAudioTimescale((Integer)conn.getAttribute("rtpmapAudioTimescale"));
			}
		}
	}
	
	/**
	 * rtsp redirect method
	 * @param request
	 * @param response
	 */
	public static void redirect(HTTPRequest request, HTTPResponse response) {
		
	}
	
	/**
	 * rtsp setup method
	 * @param request
	 * @param response
	 * @param session
	 * @throws Exception
	 */
	public static void setup(HTTPRequest request, HTTPResponse response) throws Exception {
		
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		if(conn.getAttribute("isInbound") != null) {
			setupInbound(request, response);
		} else {
			setupOutbound(request, response);
		}
	}
	
	/**
	 * rtsp setup method used of publish
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private static void setupInbound(HTTPRequest request, HTTPResponse response) throws Exception {
		
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		Matcher m = rtspTransportPattern.matcher(request.getHeader("Transport"));
		SessionDescriptionImpl sessiondescription = (SessionDescriptionImpl)conn.getAttribute("sdp");
		String mediaType = null;
		String videoControl = null;
		String audioControl = null;
		IoBuffer videoConfig = null;
		IoBuffer audioConfig = null;
		String rtpmap;
		String fmtp;
		for(MediaDescription desc : sessiondescription.getMediaDescriptions(false)) {
			mediaType = desc.getMedia().getMediaType();
			rtpmap = desc.getAttribute("rtpmap");
			fmtp = desc.getAttribute("fmtp");
			if(mediaType.equalsIgnoreCase("video")) {
				videoControl = desc.getAttribute("control");
				videoConfig = RTPUtil.decodeVideoConfigure(rtpmap, fmtp);
				conn.setAttribute("videoConfig", videoConfig);
				conn.setAttribute("rtpmapVideoTimescale", RTPUtil.decodeRtpmapVideoTimescale(rtpmap));
			} else if(mediaType.equalsIgnoreCase("audio")) {
				audioControl = desc.getAttribute("control");
				audioConfig = RTPUtil.decodeAudioConfigure(rtpmap, fmtp);
				conn.setAttribute("audioConfig", audioConfig);
				conn.setAttribute("rtpmapAudioTimescale", RTPUtil.decodeRtpmapAudioTimescale(rtpmap));
			}
		}
		
		// must video is avc and audio is aac
		if(videoConfig == null && audioConfig == null) {
			response.setStatus(RTSPResponseStatuses.BAD_REQUEST);
			return;
		}
		
		if(StringUtils.isEmpty(videoControl) && StringUtils.isEmpty(audioControl)) {
			response.setStatus(RTSPResponseStatuses.BAD_REQUEST);
			return;
		}
		
		if (m.matches()) { // udp is unsupported
			response.setStatus(RTSPResponseStatuses.UNSUPPORTED_TRANSPORT);
		} else { // tcp is support
			if(request.getUri().endsWith("streamid=0")) {		
				response.setHeader("Transport", "RTP/AVP/TCP;unicast;interleaved=0-1");//video
			}
			else if(request.getUri().endsWith("streamid=1")) { 
				response.setHeader("Transport", "RTP/AVP/TCP;unicast;interleaved=2-3");//audio
			}
		}
	}
	
	/**
	 * rtsp setup metod used of play
	 * @param request
	 * @param response
	 * @param session
	 * @throws Exception
	 */
	private static void setupOutbound(HTTPRequest request, HTTPResponse response) throws Exception {

		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		Matcher m = rtspTransportPattern.matcher(request.getHeader("Transport"));
		RTPPlayer player = conn.getRtpConnector();		
		if(m.matches()) { // udp
			InetSocketAddress localAddr = ((InetSocketAddress)conn.getRtspSession().getLocalAddress());
			String host = localAddr.getAddress().getHostAddress();
			conn.setPlayType(RTSPMinaConnection.PLAY_TYPE_UDP);
			int rtpPort = Integer.valueOf(m.group(1));
			int rtcpPort = Integer.valueOf(m.group(2));
			StringBuilder serverTransport = new StringBuilder();
			UDPPortManager udpPortMgr = UDPPortManager.getInstance();
			InetSocketAddress remoteAddress = (InetSocketAddress)conn.getRtspSession().getRemoteAddress();
			if (request.getUri().endsWith("trackID=0")) {
				int[] pair = udpPortMgr.expandToPortPair(udpPortMgr.acquireUDPPortPair());
				conn.setVideoPairPort(pair);
				while(!bindVideoPort(pair)) {
					pair = udpPortMgr.expandToPortPair(udpPortMgr.acquireUDPPortPair());
				}													
				InetSocketAddress rtpVideoAddress = new InetSocketAddress(remoteAddress.getAddress(), rtpPort);
				InetSocketAddress rtcpVideoAddress = new InetSocketAddress(remoteAddress.getAddress(), rtcpPort);
				player.setVideoRtpAddress(rtpVideoAddress);
				player.getVideoRtpPacketizer().initRtcpInfo(rtcpVideoAddress);			
				// handle mpegts
				if(conn.isMpegts()) {
					player.setAudioRtpAddress(rtpVideoAddress);
					player.getAudioRtpPacketizer().initRtcpInfo(rtcpVideoAddress);	
				}
				rtpSocketMaps.put(String.format("%s:%d", remoteAddress.getAddress().getHostAddress(), rtcpPort), player);
				serverTransport.append(request.getHeader("Transport"))
						.append(";source=").append(host)
						.append(";server_port=").append(pair[0]).append("-")
						.append(pair[1]);
			} else if (request.getUri().endsWith("trackID=1")) {
				int[] pair = udpPortMgr.expandToPortPair(udpPortMgr.acquireUDPPortPair());
				conn.setAudioPairPort(pair);
				while(!bindAudioPort(pair)) {
					pair = udpPortMgr.expandToPortPair(udpPortMgr.acquireUDPPortPair());
				}		
				InetSocketAddress rtpAudioAddress = new InetSocketAddress(remoteAddress.getAddress(), rtpPort);
				InetSocketAddress rtcpAudioAddress = new InetSocketAddress(remoteAddress.getAddress(), rtcpPort);
				player.setAudioRtpAddress(rtpAudioAddress);
				player.getAudioRtpPacketizer().initRtcpInfo(rtcpAudioAddress);	
				rtpSocketMaps.put(String.format("%s:%d", remoteAddress.getAddress().getHostAddress(), rtcpPort), player);
				serverTransport.append(request.getHeader("Transport"))
						.append(";source=").append(host)
						.append(";server_port=").append(pair[0]).append("-")
						.append(pair[1]);
			}			
			response.setHeader("Transport", serverTransport.toString());
			
		} else { //tcp
			conn.setPlayType(RTSPMinaConnection.PLAY_TYPE_TCP);
			if(request.getUri().endsWith("trackID=0")) {	
				response.setHeader("Transport", String.format("RTP/AVP/TCP;unicast;interleaved=0-1;ssrc=%d", player.getVideoRtpPacketizer().getSsrc()));//视频
			} else if(request.getUri().endsWith("trackID=1")) { 
				response.setHeader("Transport", String.format("RTP/AVP/TCP;unicast;interleaved=2-3;ssrc=%d", player.getAudioRtpPacketizer().getSsrc()));//音频
			}				
		}
	}
	
	/**
	 * bind video udp transport port include rtp and rtcp
	 * @param pair
	 * @return
	 */
	private static boolean bindVideoPort(int[] pair) {

		boolean flag = true;
		try {
			RTSPMinaTransport.RTP_VIDEO_ACCEPTOR.bind(new InetSocketAddress("127.0.0.1", pair[0]));
			RTSPMinaTransport.RTCP_VIDEO_ACCEPTOR.bind(new InetSocketAddress("127.0.0.1", pair[1]));
		} catch (IOException e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * bind audio udp transport port include rtp and rtcp
	 * @param pair
	 * @return
	 */
	private static boolean bindAudioPort(int[] pair) {

		boolean flag = true;
		try {
			RTSPMinaTransport.RTP_AUDIO_ACCEPTOR.bind(new InetSocketAddress("127.0.0.1", pair[0]));
			RTSPMinaTransport.RTCP_AUDIO_ACCEPTOR.bind(new InetSocketAddress("127.0.0.1", pair[1]));
		} catch (IOException e) {
			flag = false;
		}
		return flag;
	}
	
	public static void teardown(HTTPRequest request, HTTPResponse response) {
		
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		conn.close();
	}
	
	/**
	 * by avc config and aac config product sdp 
	 * @param videoConfig
	 * @param audioConfig
	 * @param session
	 * @return
	 * @throws SdpException
	 */
	public static String configureSDP() throws Exception {
		
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		SessionDescription sdp;
		MediaDescriptionImpl mdvideo = null;//video md
		MediaDescriptionImpl mdaudio = null;//audio md
		Vector<MediaDescription> mds = new Vector<MediaDescription>(1);
		Vector<Attribute> attr = new Vector<Attribute>();
		sdp = SdpFactory.getInstance().createSessionDescription();
		InetSocketAddress remoteAddress = (InetSocketAddress)conn.getRtspSession().getRemoteAddress();
		String remoteIp = "";
		if(remoteAddress != null && remoteAddress.getAddress() != null) {
			remoteIp = remoteAddress.getAddress().toString().substring(1);
		}		
		sdp.setOrigin(SdpFactory.getInstance().createOrigin("sms", conn.getSessionId(), "1.0", "IN", "IPV4", remoteIp));
		sdp.setAttributes(attr);
		sdp.setConnection(SdpFactory.getInstance().createConnection("IN", "IPV4", remoteIp));
		CustomSingleItemSubStream rtspStream = (CustomSingleItemSubStream)conn.getAttribute("rtspStream");
				
		IoBuffer data = null;
		byte codecId;
		RTPPlayer player;
		IRTPPacketizer videoRtpPacketizer = null;
		IRTPPacketizer audioRtpPacketizer = null;
		
		IoBuffer videoConfig = IoBuffer.allocate(128).setAutoExpand(true);
		IoBuffer audioConfig = IoBuffer.allocate(128).setAutoExpand(true);
		AtomicLong duration = new AtomicLong(0);
		rtspStream.getConfig(videoConfig, audioConfig, duration);
		if (duration.get() == 0) {
			attr.add(SdpFactory.getInstance().createAttribute("range", "npt=now-"));
			conn.setLive(true);
		} else {
			attr.add(SdpFactory.getInstance().createAttribute("range", "npt=0-" + (double)duration.get() / 1000D));
			conn.setLive(false);
		}
		
		if(conn.isMpegts()) {
			videoRtpPacketizer = new RTPPacketizerMPEGTS(videoConfig, audioConfig);
			audioRtpPacketizer = videoRtpPacketizer;
			mdvideo = videoRtpPacketizer.getDescribeInfo(data);
			if(mdvideo != null) {
				mdvideo.setAttribute("control", "trackID=0");				
				mds.add(mdvideo);
			}
		} else {
			 // configure video sdp				
			data = videoConfig.asReadOnlyBuffer();
			data.mark();
			codecId = (byte)FLVUtils.getVideoCodec(data.get());
			data.position(5);
			switch (codecId) {
			case 0x07: //avc/h.264 video
				videoRtpPacketizer = new RTPPacketizerRFC3984H264();
				break;
			}
			
			if(videoRtpPacketizer != null) {
				mdvideo = videoRtpPacketizer.getDescribeInfo(data);
			}
			
			if(mdvideo != null) {
				mdvideo.setAttribute("control", "trackID=0");				
				mds.add(mdvideo);
			}
			
			// configure audio sdp				
			data = audioConfig.asReadOnlyBuffer();
			data.mark();
			codecId = (byte)FLVUtils.getAudioCodec(data.get());			
			data.skip(1);
			switch (codecId) {
			case 0x02: //mp3
				audioRtpPacketizer = new RTPPacketizerRFC2250MP3(); 
				break;
			case 0x0a: //aac
				audioRtpPacketizer = new RTPPacketizerMPEG4AAC();				
				break;
			}
			
			if(audioRtpPacketizer != null) {
				mdaudio = audioRtpPacketizer.getDescribeInfo(data);
			}
			
			if(mdaudio != null) {
				mdaudio.setAttribute("control", "trackID=1");				
				mds.add(mdaudio);
			}
		}
		player = new RTPPlayer(conn, videoRtpPacketizer, audioRtpPacketizer);
		conn.setRtpConnector(player);
		sdp.setMediaDescriptions(mds);
		return sdp.toString();			
	}
	
	/**
	 * parse rtsp url and create publish stream 
	 * @param url
	 * @param session
	 * @return
	 * @throws URISyntaxException
	 */
	private static RTSPPushProxyStream createPublishStream(String url) throws URISyntaxException {
		
		URI uri = new URI(url);		
		String[] segments = uri.getPath().substring(1).split("/");
		if (segments.length < 2) return null;
			
		String app = segments[0];
		String stream = segments[1];
		Scope scope = ScopeUtils.getScope(app);	
		if (scope == null || StringUtils.isEmpty(stream)) return null;
		
		RTSPPushProxyStream pubStream = new RTSPPushProxyStream(stream);
		pubStream.setScope(scope);
		return pubStream;
	}
	
	/**
	 * parse rtsp url and create play stream
	 * @param url
	 * @param session
	 * @return
	 * @throws URISyntaxException
	 */
	private static RTSPConnectionConsumer createPlayStream(String url) throws URISyntaxException {
		
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
		URI uri = new URI(queryStringDecoder.getPath());		
		String[] segments = uri.getPath().substring(1).split("/");
		if (segments.length < 2) return null;
			
		String app = segments[0];
		String stream = segments[1];
		
		Scope scope = ScopeUtils.getScope(app);	
		if (scope == null || StringUtils.isEmpty(stream)) return null;
		
		StringBuilder streamSb = new StringBuilder();
		streamSb.append(stream);
		boolean firstParam = true;
		// fix stream 
		// rtsp://192.168.10.123:5544/live/12345?tcp&mpegts&starttime=12345
		if (!queryStringDecoder.getParameters().isEmpty()) {
			Map<String, List<String>> params = queryStringDecoder.getParameters();
			for (String key : params.keySet()) {
				if (key.equalsIgnoreCase("tcp")) {
					continue;
				} else if (key.equalsIgnoreCase("mpegts")) {
					conn.setMpegts(true);
				} else {
					if (params.get(key).size() > 0) {
						if (firstParam) {
							streamSb.append("?");
							firstParam = false;
						} else {
							streamSb.append("&");
						}
						streamSb.append(key).append("=").append(params.get(0));
					}
				}
			}
		}
		
		// play security
		IStreamSecurityService security = (IStreamSecurityService) ScopeUtils.getScopeService(scope, IStreamSecurityService.class);
		if (security != null) {
			Set<IStreamPlaybackSecurity> handlers = security.getStreamPlaybackSecurity();
			for (IStreamPlaybackSecurity handler : handlers) {
				if (!handler.isPlaybackAllowed(scope, stream, 0, 0, false)) {
					return null;
				}
			}
		}

		// set up stream
		RTSPConnectionConsumer rtspConsumer = new RTSPConnectionConsumer(conn);
		rtspConsumer.getConnection().connect(scope);
		CustomSingleItemSubStream rtspStream = new CustomSingleItemSubStream(scope, rtspConsumer);	
		SimplePlayItem playItem = SimplePlayItem.build(stream, -2000, -1);
		rtspStream.setPlayItem(playItem);
		rtspStream.start();
		
		conn.setAttribute("rtspStream", rtspStream);
		
		try {
			rtspStream.play();
		} catch (Exception e) {
			rtspStream.stop();
			return null;
		}
		
		if (rtspStream.isFailure()) {
			rtspStream.stop();
			return null;
		}

		return rtspConsumer;		
	}
}
