package com.sms.server.net.rtsp;

import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.CONTENT_TYPE;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.CSEQ;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.PUBLIC;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.SESSION;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.TRANSPORT;
import static com.sms.server.net.rtsp.message.RTSPMethods.DESCRIBE;
import static com.sms.server.net.rtsp.message.RTSPMethods.OPTIONS;
import static com.sms.server.net.rtsp.message.RTSPMethods.PLAY;
import static com.sms.server.net.rtsp.message.RTSPMethods.SETUP;
import static com.sms.server.net.rtsp.message.RTSPResponseStatuses.OK;
import static com.sms.server.net.rtsp.message.RTSPVersions.RTSP_1_0;
import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import javax.sdp.MediaDescription;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.SMS;
import com.sms.server.net.http.codec.HTTPCodecUtil;
import com.sms.server.net.http.message.DefaultHttpRequest;
import com.sms.server.net.http.message.HTTPMethod;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.rtp.RTPPacket;
import com.sms.server.net.rtp.RTPUtil;
import com.sms.server.net.rtsp.codec.RTSPClientCodecFactory;
import com.sms.server.net.rtsp.message.RTSPChannelData;
import com.sms.server.stream.proxy.RTSPProxyStream;

/**
 * rtsp client(rtp over tcp)
 * @author pengliren
 *
 */
public class RTSPClient extends IoHandlerAdapter {

	private static final Logger log = LoggerFactory.getLogger(RTSPClient.class);

	protected static final int CONNECTOR_WORKER_TIMEOUT = 7000; // seconds

	// Socket connector, disposed on disconnect
	protected SocketConnector socketConnector;

	protected ConnectFuture future;
	
	private final String playUrl; 
	
	private String ip;
	
	private int port;
	
	private String[] methods;
	
	private volatile boolean setupVideo = true;
	
	private volatile boolean setupAudio = true;
	
	private String videoControl = null;
	
	private String audioControl = null;
	
	private byte videoRtpChannel = 0;
	
	private byte audioRtpChannel = 2;
	
	private byte videoRtcpChannel = 1;
	
	private byte audioRtcpChannel = 3;
	
	private String playStream;
	
	private RTSPProxyStream proxyStream;
	
	public RTSPClient(String url, RTSPProxyStream proxyStream) {
		this.playUrl = url;
		URI uri = null;
		try {
			uri = new URI(url);
			ip = uri.getHost();
			port = uri.getPort();
			playStream = uri.getPath();
			this.proxyStream = proxyStream;
		} catch (URISyntaxException e) {
			log.error("rtsp play url is error  {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	public void startConnector() {
		socketConnector = new NioSocketConnector();		
		socketConnector.setHandler(this);
		future = socketConnector.connect(new InetSocketAddress(ip, port));
		future.addListener(new IoFutureListener<ConnectFuture>() {
			public void operationComplete(ConnectFuture future) {
				try {
					// will throw RuntimeException after connection error
					future.getSession();
				} catch (Throwable e) {
					socketConnector.dispose(false);
				}
			}
		});
		// Now wait for the close to be completed
		future.awaitUninterruptibly(CONNECTOR_WORKER_TIMEOUT);
	}
	
	private void startRequest(RTSPMinaConnection conn) {
		HTTPRequest request = new DefaultHttpRequest(RTSP_1_0, OPTIONS, playStream);
		request.addHeader("CSeq", 1);
		conn.setAttribute("method", OPTIONS);
		conn.write(request);
	}
	
	private boolean availableMethod(String method) {
		
		boolean flag = false;
		for(int i = 0; i < methods.length; i++) {
			if(method.equals(methods[i].trim())) {
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	public void disconnect() {
		if (future != null) {
			try {
				// close requesting that the pending messages are sent before the session is closed
				future.getSession().close(false);
				// now wait for the close to be completed
				future.awaitUninterruptibly(CONNECTOR_WORKER_TIMEOUT);
			} catch (Exception e) {
				log.warn("Exception during disconnect", e);
			} finally {
				// We can now dispose the connector
				socketConnector.dispose(false);
			}
		}
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		
		RTSPMinaConnection conn = (RTSPMinaConnection)session.getAttribute(RTSPMinaConnection.RTSP_CONNECTION_KEY);
		if(conn != null) conn.close();
		
		if(cause instanceof ProtocolCodecException) {
			log.warn("Exception caught {}", cause.getMessage());
		} else {
			log.error("Exception caught {}", cause.getMessage());
			session.close(false);
		}
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {

		RTSPMinaConnection conn = (RTSPMinaConnection)session.getAttribute(RTSPMinaConnection.RTSP_CONNECTION_KEY);
		SMS.setConnectionLocal(conn);
		if(message instanceof HTTPResponse) {
			
			HTTPResponse response = (HTTPResponse)message;
			HTTPRequest request;
			if(response.getStatus().equals(OK)) {
				
				HTTPMethod method = (HTTPMethod)conn.getAttribute("method");
				if(method == OPTIONS){
					
					String publish = response.getHeader(PUBLIC);
					methods = publish.split(",");
					if(!availableMethod(DESCRIBE.getName())) {session.close(true); return;}
					request = new DefaultHttpRequest(RTSP_1_0, DESCRIBE, playStream);
					conn.setAttribute("method", DESCRIBE);
					request.addHeader(SESSION, String.valueOf(session.getId()));
					request.addHeader(CSEQ, Integer.parseInt(response.getHeader("CSeq"))+1);
					conn.write(request);
				} else if(method == DESCRIBE) {
					if(!availableMethod(SETUP.getName())) {session.close(true); return;}
					String contentType = response.getHeader(CONTENT_TYPE);
					if(contentType.equals("application/sdp")) {
						String sdp = HTTPCodecUtil.decodeBody(response.getContent());
						String mediaType = null;
						
						// get the first video track and audio track
						if(sdp != null) {
							SDPAnnounceParser parser = new SDPAnnounceParser (sdp);
							SessionDescriptionImpl sessiondescription = parser.parse();
							IoBuffer videoConfig = null;
							IoBuffer audioConfig = null;
							String rtpmap;
							String fmtp;
							for(MediaDescription desc : sessiondescription.getMediaDescriptions(false)) {
								mediaType = desc.getMedia().getMediaType();
								rtpmap = desc.getAttribute("rtpmap");
								fmtp = desc.getAttribute("fmtp");
								if(mediaType.equalsIgnoreCase("video")) {
									setupVideo = false;
									videoControl = desc.getAttribute("control");
									videoConfig = RTPUtil.decodeVideoConfigure(rtpmap, fmtp);
									conn.setAttribute("videoConfig", videoConfig);
									conn.setAttribute("rtpmapVideoTimescale", RTPUtil.decodeRtpmapVideoTimescale(rtpmap));
									conn.setAttribute("videoConfig", videoConfig);
								} else if(mediaType.equalsIgnoreCase("audio")) {
									setupAudio = false;
									audioControl = desc.getAttribute("control");
									audioConfig = RTPUtil.decodeAudioConfigure(rtpmap, fmtp);
									conn.setAttribute("audioConfig", audioConfig);
									conn.setAttribute("rtpmapAudioTimescale", RTPUtil.decodeRtpmapAudioTimescale(rtpmap));
									conn.setAttribute("audioConfig", audioConfig);
								}
							}
						}
						StringBuilder urlBuilder = new StringBuilder();
						urlBuilder.append(playStream).append("/").append(videoControl);
						request = new DefaultHttpRequest(RTSP_1_0, SETUP, videoControl);
						request.addHeader(TRANSPORT, String.format("RTP/AVP/TCP;unicast;interleaved=%d-%d", videoRtpChannel, videoRtcpChannel));
						conn.setAttribute("method", SETUP);
						request.addHeader(SESSION, String.valueOf(session.getId()));
						request.addHeader(CSEQ, Integer.parseInt(response.getHeader("CSeq"))+1);
						conn.write(request);
						
						urlBuilder = new StringBuilder();
						urlBuilder.append(playStream).append("/").append(audioControl);
						request = new DefaultHttpRequest(RTSP_1_0, SETUP, audioControl);
						request.addHeader(TRANSPORT, String.format("RTP/AVP/TCP;unicast;interleaved=%d-%d", audioRtpChannel, audioRtcpChannel));
						conn.setAttribute("method", SETUP);
						request.addHeader(SESSION, String.valueOf(session.getId()));
						request.addHeader(CSEQ, Integer.parseInt(response.getHeader("CSeq"))+1);
						conn.write(request);
						
					} else {
						session.close(true);
						return;
					}
				} else if(method == SETUP) {
					
					String transport = response.getHeader(TRANSPORT);
					String videoTransport = String.format("%d-%d", videoRtpChannel, videoRtcpChannel);
					String audioTransport = String.format("%d-%d", audioRtpChannel, audioRtcpChannel);
					if(transport.contains(videoTransport)) {
						setupVideo = true;
					} else if(transport.contains(audioTransport)) {
						setupAudio = true;
					} else {
						session.close(true);
						return;
					}
					
					if(setupVideo && setupAudio) {
						request = new DefaultHttpRequest(RTSP_1_0, PLAY, playStream);
						conn.setAttribute("method", PLAY);
						request.addHeader(SESSION, String.valueOf(session.getId()));
						request.addHeader(CSEQ, Integer.parseInt(response.getHeader("CSeq"))+1);
						conn.write(request);
					}
				} else if(method == PLAY) {

					// we must check video is h264 and audio is aac	
					IoBuffer videoConfig = (IoBuffer)conn.getAttribute("videoConfig");
					if(videoConfig != null) {
						proxyStream.setAVCConfig(videoConfig);
						proxyStream.setVideoTimescale((Integer)conn.getAttribute("rtpmapVideoTimescale"));
					}
					
					IoBuffer audioConfig = (IoBuffer)conn.getAttribute("audioConfig");
					if(audioConfig != null) {
						proxyStream.setAACConfig(audioConfig);
						proxyStream.setAudioTimescale((Integer)conn.getAttribute("rtpmapAudioTimescale"));
					}
				}
				
			} else {
				session.close(true);
			}
			
		} else if(message instanceof RTSPChannelData) { // handle rtp data
						
			RTSPChannelData rtspData = (RTSPChannelData)message;			
			RTPPacket rtp = new RTPPacket(rtspData.getData());
			rtp.setChannel(rtspData.getChannel());
			proxyStream.handleMessage(rtp);			
		}
		SMS.setConnectionLocal(null);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {

		// check play stram is null
		RTSPMinaConnection conn = (RTSPMinaConnection)session.getAttribute(RTSPMinaConnection.RTSP_CONNECTION_KEY);
		
		// clear rtsp session
		if(conn != null) {			
			conn.close();
		}
		proxyStream.close();
		log.info("Session Closed");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {

		log.info("Session Created");			
		session.getFilterChain().addLast("protocolFilter", new ProtocolCodecFilter(new RTSPClientCodecFactory()));
		
		// create rtsp connection
		RTSPMinaConnection conn = new RTSPMinaConnection(session);
		
		// add to session
		session.setAttribute(RTSPMinaConnection.RTSP_CONNECTION_KEY, conn);
		
		startRequest(conn);
	}
	
	public String getPlayUrl() {
		return playUrl;
	}
}
