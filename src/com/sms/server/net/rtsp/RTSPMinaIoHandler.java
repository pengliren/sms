package com.sms.server.net.rtsp;

import static com.sms.server.net.http.message.HTTPMethod.GET;
import static com.sms.server.net.http.message.HTTPMethod.POST;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.ACCEPT;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.CONTENT_TYPE;
import static com.sms.server.net.rtsp.message.RTSPResponseStatuses.OK;
import static com.sms.server.net.rtsp.message.RTSPVersions.RTSP_1_0;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.SMS;
import com.sms.server.net.http.message.DefaultHttpResponse;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.rtp.RTPPacket;
import com.sms.server.net.rtsp.codec.RTSPServerCodecFactory;
import com.sms.server.net.rtsp.message.RTSPChannelData;
import com.sms.server.net.rtsp.message.RTSPResponseStatuses;
import com.sms.server.stream.proxy.RTSPPushProxyStream;

/**
 * RTSP Mina Io Handler
 * @author pengliren
 *
 */
public class RTSPMinaIoHandler extends IoHandlerAdapter {
	
	private static Logger log = LoggerFactory.getLogger(RTSPMinaIoHandler.class);
	
	public RTSPMinaIoHandler() {
	
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
		// handle rtsp method
		if(message instanceof HTTPRequest){			
			boolean isResponse = true; // need send response ? rtsp default send if rtsp tunnelled is not send 
			HTTPRequest request = (HTTPRequest)message;
			HTTPResponse response  = new DefaultHttpResponse(RTSP_1_0, OK);
					
			// first handle rtsp method
			boolean flag = RTSPCore.handleRtspMethod(request, response);
			
			if(!flag) {
				// second check rtsp over http tunnel
				if (request.getMethod().equals(GET)
						&& request.getHeader(ACCEPT) != null
						&& request.getHeader(ACCEPT).equalsIgnoreCase(
								RTSPTunnel.RTSP_TUNNELLED)) {
					isResponse = false;
					// rtsp over http for get
					RTSPTunnel.get(request, response);
				} else if (request.getMethod().equals(POST)
						&& request.getHeader(CONTENT_TYPE) != null
						&& request.getHeader(CONTENT_TYPE).equalsIgnoreCase(
								RTSPTunnel.RTSP_TUNNELLED)) {
					isResponse = false;
					// rtsp over http for post
					RTSPTunnel.post(request, response);
				} else {
					log.info("not support method {}", request);
					response.setStatus(RTSPResponseStatuses.BAD_REQUEST);
				}
			}
			if(isResponse) conn.write(response);
		} else if(message instanceof RTSPChannelData) { // handle rtsp or rtcp data may be rtsp publish stream
			RTSPChannelData channelData = (RTSPChannelData)message;
			byte channel = channelData.getChannel();
			IoBuffer data = channelData.getData();
			if(channel == 0x01 || channel == 0x03) {//rtcp
				//RTCPPacket rtcpPkt = new RTCPPacket();
				//rtcpPkt.decode(data);
				//log.info("rtcp packet {}", rtcpPkt);
			} else {//rtp
				//TODO we need add timescale from sdp parse, but current also not add; 
				RTPPacket rtpPkt = new RTPPacket(data);
				rtpPkt.setChannel(channel);
				//log.info("rtp packet channel {}, len {}, ts {}", new Object[]{rtpPkt.getChannel(), rtpPkt.getPayload().length, rtpPkt.getTimestamp().longValue()});
				RTSPPushProxyStream pushStream = (RTSPPushProxyStream)conn.getAttribute("pushStream");
				if(pushStream != null) {
					pushStream.handleMessage(rtpPkt);
				}
			}
		}			
		SMS.setConnectionLocal(null);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {

		log.debug("RTSP Session Closed");
		
		// check play stram is null
		RTSPMinaConnection conn = (RTSPMinaConnection) session.getAttribute(RTSPMinaConnection.RTSP_CONNECTION_KEY);
		if (conn != null) {
			conn.close();
		}
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {

		log.debug("RTSP Session Created");			
		session.getFilterChain().addLast("protocolFilter", new ProtocolCodecFilter(new RTSPServerCodecFactory()));
		
		// create rtsp connection
		RTSPMinaConnection conn = new RTSPMinaConnection(session);
		
		// add to session
		session.setAttribute(RTSPMinaConnection.RTSP_CONNECTION_KEY, conn);
	}
}
