package com.sms.server.net.rtsp.codec;

import com.sms.server.net.http.message.DefaultHttpResponse;
import com.sms.server.net.http.message.HTTPMessage;
import com.sms.server.net.http.message.HTTPResponseStatus;
import com.sms.server.net.rtsp.message.RTSPVersions;

/**
 * RTSP Response Decoder
 * @author pengliren
 *
 */
public class RTSPResponseDecoder extends RTSPMessageDecoder {

	@Override
	protected HTTPMessage createMessage(String[] initialLine) throws Exception {

		return new DefaultHttpResponse(RTSPVersions.valueOf(initialLine[0]), 
										new HTTPResponseStatus(Integer.valueOf(initialLine[1]), initialLine[2]));
	}
}
