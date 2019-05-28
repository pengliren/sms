package com.sms.server.net.http;

import static com.sms.server.net.http.message.HTTPHeaders.isKeepAlive;
import static com.sms.server.net.http.message.HTTPHeaders.Names.CONNECTION;
import static com.sms.server.net.http.message.HTTPHeaders.Names.CONTENT_LENGTH;
import static com.sms.server.net.http.message.HTTPHeaders.Values.KEEP_ALIVE;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.SMS;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.http.message.HTTPResponseStatus;

/**
 * Base HTTP Service
 * @author pengliren
 *
 */
public abstract class BaseHTTPService implements IHTTPService {
	
	protected static Logger log = LoggerFactory.getLogger(BaseHTTPService.class);
	
	/**
	 * HTTP request method to use for HTTP calls.
	 */
	protected static final String REQUEST_GET_METHOD = "GET";
	
	protected static final String REQUEST_POST_METHOD = "POST";
	
	@Override
	public void sendError(HTTPRequest req, HTTPResponse resp, HTTPResponseStatus status) {
		commitResponse(req, resp, null, status);
		writeData(false, resp, true);
	}
	
	@Override
	public void commitResponse(HTTPRequest req, HTTPResponse resp, IoBuffer data) {
		
		commitResponse(req, resp, data, HTTPResponseStatus.OK);
	}
	
	@Override
	public void commitResponse(HTTPRequest req, HTTPResponse resp, IoBuffer data, HTTPResponseStatus status) {	
		
		resp.setStatus(status);
		
		if(data != null && data.remaining() > 0) {
			resp.addHeader(CONTENT_LENGTH, data.remaining());
			resp.setContent(data);
		}
		
		boolean isKeepAlive = isKeepAlive(req); 
		
		if(isKeepAlive) {
			resp.setHeader(CONNECTION, KEEP_ALIVE);
		}
		
		writeData(isKeepAlive, resp, false);
	}
	
	private void writeData(boolean isKeepAlive, HTTPResponse resp, boolean isClose) {
		
		HTTPMinaConnection conn = (HTTPMinaConnection)SMS.getConnectionLocal();	
		
		WriteFuture future = conn.write(resp);
		
		if(isClose || !isKeepAlive) {
			future.addListener(new IoFutureListener<IoFuture>() {
				@Override
				public void operationComplete(IoFuture future) {
					future.getSession().close(true);
				}
			});
		} 
	}
	
	@Override
	public void start() {
		
	}
	
	@Override
	public void stop() {
		
	}
}
