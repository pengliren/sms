package com.sms.server.net.rtsp;

import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.CACHE_CONTROL;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.CONNECTION;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.CONTENT_TYPE;
import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.SERVER;
import static com.sms.server.net.rtsp.message.RTSPResponseStatuses.OK;
import static com.sms.server.net.rtsp.message.RTSPVersions.RTSP_1_0;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.SMS;
import com.sms.server.net.http.codec.DecodeState;
import com.sms.server.net.http.codec.HTTPCodecUtil;
import com.sms.server.net.http.message.DefaultHttpResponse;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.http.message.HTTPResponseStatus;
import com.sms.server.net.http.message.HTTPVersion;
import com.sms.server.net.rtsp.codec.RTSPRequestDecoder;

/**
 * RTSP Over HTTP Tunnel
 * @author pengliren
 *
 */
public class RTSPTunnel {

	private static Logger log = LoggerFactory.getLogger(RTSPTunnel.class);
	
	public static final String RTSP_TUNNELLED = "application/x-rtsp-tunnelled";
	
	public static ConcurrentHashMap<String, RTSPMinaConnection> RTSP_TUNNEL_CONNS = new ConcurrentHashMap<String, RTSPMinaConnection>();
	
	/**
	 * Tunnelling RTSP and RTP through HTTP GET Request
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	public static void get(HTTPRequest request, HTTPResponse response) throws IOException {
		
		// get connection			
		RTSPMinaConnection conn = (RTSPMinaConnection)SMS.getConnectionLocal();
		
		// init DefaultHttpResponse
		response  = new DefaultHttpResponse(HTTPVersion.HTTP_1_0, OK);
		response.setHeader(SERVER, "SMS");
		response.setHeader(CACHE_CONTROL, "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setHeader(CONNECTION, "close");
		response.setHeader(CONTENT_TYPE, RTSP_TUNNELLED);
		
		// Version
		// All requests are made using HTTP version 1.0. This is to get through as many firewalls as possible. 
		HTTPVersion version = request.getProtocolVersion();
		
		// Binding the Channels
		//Each client HTTP request must include a “x-sessioncookie” header with an ID as its value. 
		//This makes it possible for the server to unambiguously bind the 2 channels. 
		//This protocol uses the value as a simple opaque token. 
		//Tokens must be unique to the server, but do not need to be globally unique.
		String sessioncookie = request.getHeader("x-sessioncookie");
		
		if(!version.equals(HTTPVersion.HTTP_1_0) || StringUtils.isEmpty(sessioncookie)) {
			response.setStatus(HTTPResponseStatus.BAD_REQUEST);
			log.info("rtsp tunnel version: {} and sessioncookie: {}", version, sessioncookie);
		} else {
			// we must bind this channel
			RTSP_TUNNEL_CONNS.put(sessioncookie, conn);
			conn.setAttribute("sessioncookie", sessioncookie);
			log.info("rtsp tunnel session cookie {}", sessioncookie);
		}
		
		conn.write(response);
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	public static void post(HTTPRequest request, HTTPResponse response) throws Exception {
		
		// get Version 
		HTTPVersion version = request.getProtocolVersion();
		
		// get sessioncookie
		String sessioncookie = request.getHeader("x-sessioncookie");
		
		RTSPMinaConnection getConn = null;
		RTSPMinaConnection postConn = (RTSPMinaConnection)SMS.getConnectionLocal();
		
		if(!version.equals(HTTPVersion.HTTP_1_0) || StringUtils.isEmpty(sessioncookie)) {
			response.setStatus(HTTPResponseStatus.BAD_REQUEST);
		} else {
			// get bind channel
			getConn = RTSP_TUNNEL_CONNS.get(sessioncookie);
		}
		
		// check bind conn
		if(getConn == null) {
			postConn.close();
			return;
		}
		
		IoBuffer lastBuffer = null;
		if(postConn.hasAttribute("rtsppostpkt")) {
			lastBuffer = (IoBuffer)postConn.getAttribute("rtsppostpkt");
		}
		
		// set local conn is getConn
		SMS.setConnectionLocal(getConn);
		String content = HTTPCodecUtil.decodeBody(request.getContent());
		IoBuffer buffer = IoBuffer.wrap(Base64.decodeBase64(content));
		RTSPRequestDecoder decoder = new RTSPRequestDecoder();
		
		if (lastBuffer != null) {
			lastBuffer.put(buffer);
			lastBuffer.flip();
		}
		else {
			lastBuffer = buffer;
		}
		
		int pos = 0;
		DecodeState obj = null;
		while(lastBuffer.hasRemaining()) {
			pos = lastBuffer.position();
			obj = decoder.decodeBuffer(lastBuffer);
			// we must rtsp packet 
			if(obj.getState() == DecodeState.ENOUGH) {
				HTTPRequest req = (HTTPRequest)obj.getObject();
				HTTPResponse resp  = new DefaultHttpResponse(RTSP_1_0, OK);
				RTSPCore.handleRtspMethod(req, resp);
				getConn.write(resp);
			} else {
				// current data not enough we must reset and wait next data
				log.info("data not enough ? ");
				lastBuffer.position(pos);
				IoBuffer temp = IoBuffer.allocate(lastBuffer.remaining()).setAutoExpand(true);
				temp.put(lastBuffer);
				postConn.setAttribute("rtsppostpkt", temp);
				break;
			}
		}
		
		// check last data is enough we must clear lastbuffer data
		if(obj != null && obj.getState() == DecodeState.ENOUGH) {
			postConn.removeAttribute("rtsppostpkt");
		}
	}
}
