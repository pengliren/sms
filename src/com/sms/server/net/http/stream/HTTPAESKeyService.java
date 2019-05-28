package com.sms.server.net.http.stream;

import static com.sms.server.net.http.message.HTTPHeaders.Names.CONTENT_TYPE;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.api.IScope;
import com.sms.server.net.http.BaseHTTPService;
import com.sms.server.net.http.IHTTPService;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.http.message.HTTPResponseStatus;

public class HTTPAESKeyService extends BaseHTTPService implements IHTTPService {

	@Override
	public void setHeader(HTTPResponse resp) {
		resp.addHeader(CONTENT_TYPE, "binary/octet-stream");
		resp.addHeader("Pragma", "no-cache"); 
		resp.setHeader("Cache-Control", "no-cache");
	}

	@Override
	public void handleRequest(HTTPRequest req, HTTPResponse resp, IScope scope) throws Exception {

		String method = req.getMethod().toString();
		if (!REQUEST_GET_METHOD.equalsIgnoreCase(method) && !REQUEST_POST_METHOD.equalsIgnoreCase(method)) {
			// Bad request - return simple error page
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			return;
		}
		String path = req.getPath().substring(1);
		String[] segments = path.split("/");
		String app = scope.getName();
		String streamName;
		if (segments.length < 2) { // stream/playlist.m3u8
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);		
			return;
		}
		streamName = segments[0];		
				
		MpegtsSegmenterService service = MpegtsSegmenterService.getInstance();
		if(service.isAvailable(scope, streamName)) {
			String encKey = service.getSegmentEnckey(app, streamName);
			if(!StringUtils.isEmpty(encKey)) {
				int len = encKey.length() / 2;
				byte[] keyBuffer = new byte[len];  
				for (int i = 0; i < len; i++) keyBuffer[i] = (byte)Integer.parseInt(encKey.substring(i*2, (i*2)+2), 16);
				IoBuffer data = IoBuffer.wrap(keyBuffer);
				setHeader(resp);
				commitResponse(req, resp, data);
			} else {
				sendError(req, resp, HTTPResponseStatus.FORBIDDEN);
			}
		} else {
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
		}
	}
}
