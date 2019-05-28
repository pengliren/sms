package com.sms.server.net.http;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.api.IScope;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.http.message.HTTPResponseStatus;

/**
 * HTTP Service Interface
 * @author pengliren
 *
 */
public interface IHTTPService {
	
	public void sendError(HTTPRequest req, HTTPResponse resp, HTTPResponseStatus status);
	
	public void setHeader(HTTPResponse resp);

	public void handleRequest(HTTPRequest req, HTTPResponse resp, IScope scope) throws Exception;
	
	public void commitResponse(HTTPRequest req, HTTPResponse resp, IoBuffer data, HTTPResponseStatus status);
	
	public void commitResponse(HTTPRequest req, HTTPResponse resp, IoBuffer data);

    public void start();

    public void stop();
}
