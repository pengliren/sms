package com.sms.server.net.http.stream;

import java.util.Set;

import com.sms.server.api.IScope;
import com.sms.server.api.SMS;
import com.sms.server.api.ScopeUtils;
import com.sms.server.api.stream.IClientStream;
import com.sms.server.api.stream.IStreamPublishSecurity;
import com.sms.server.api.stream.IStreamSecurityService;
import com.sms.server.net.http.BaseHTTPService;
import com.sms.server.net.http.HTTPMinaConnection;
import com.sms.server.net.http.IHTTPService;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.http.message.HTTPResponseStatus;
import com.sms.server.stream.proxy.HTTPPushProxyStream;

public class HTTPLiveFlvPublisherService extends BaseHTTPService implements IHTTPService {

	@Override
	public void setHeader(HTTPResponse resp) {

		resp.addHeader("Pragma", "no-cache"); 
		resp.setHeader("Cache-Control", "no-cache");
	}

	@Override
	public void handleRequest(HTTPRequest req, HTTPResponse resp, IScope scope) throws Exception {

		HTTPMinaConnection conn = (HTTPMinaConnection)SMS.getConnectionLocal();
		String path = req.getPath().substring(1);
		String[] segments = path.split("/");
		if(!REQUEST_POST_METHOD.equalsIgnoreCase(req.getMethod().toString()) || segments.length < 2) {
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			return;
		}
		String streamName = segments[1];
		
		// publish security
		IStreamSecurityService security = (IStreamSecurityService) ScopeUtils.getScopeService(scope, IStreamSecurityService.class);
		if (security != null) {
			Set<IStreamPublishSecurity> handlers = security.getStreamPublishSecurity();
			for (IStreamPublishSecurity handler : handlers) {
				if (!handler.isPublishAllowed(scope, streamName, IClientStream.MODE_LIVE)) {
					sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
					return;
				}
			}
		}
		
		HTTPPushProxyStream pushStream = new HTTPPushProxyStream(streamName);
		pushStream.setScope(scope);
		pushStream.start();
		conn.setAttribute("pushStream", pushStream);
		conn.write(resp);
	}
}
