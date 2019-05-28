package com.sms.server.net.http.stream;

import static com.sms.server.net.http.message.HTTPHeaders.Names.CONTENT_LENGTH;
import static com.sms.server.net.http.message.HTTPHeaders.Names.CONTENT_TYPE;

import java.io.IOException;
import java.util.Set;

import org.apache.mina.core.session.IdleStatus;

import com.sms.server.Configuration;
import com.sms.server.api.IScope;
import com.sms.server.api.SMS;
import com.sms.server.api.ScopeUtils;
import com.sms.server.api.stream.IStreamPlaybackSecurity;
import com.sms.server.api.stream.IStreamSecurityService;
import com.sms.server.api.stream.support.SimplePlayItem;
import com.sms.server.net.http.BaseHTTPService;
import com.sms.server.net.http.HTTPMinaConnection;
import com.sms.server.net.http.IHTTPService;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.http.message.HTTPResponseStatus;

/**
 * HTTP Flv Stream Player Service
 * @author pengliren
 *
 */
public class HTTPFlvPlayerService extends BaseHTTPService implements IHTTPService {

	@Override
	public void setHeader(HTTPResponse resp) {

		resp.addHeader("Accept-Ranges", "bytes");
		resp.addHeader(CONTENT_TYPE, "video/x-flv");
		resp.addHeader(CONTENT_LENGTH, Integer.MAX_VALUE);  
		resp.addHeader("Pragma", "no-cache"); 
		resp.setHeader("Connection", "Keep-Alive");
		resp.setHeader("Cache-Control", "no-cache");
	}

	@Override
	public void handleRequest(HTTPRequest req, HTTPResponse resp, IScope scope) throws Exception {

		HTTPMinaConnection conn = (HTTPMinaConnection)SMS.getConnectionLocal();
		if (!REQUEST_GET_METHOD.equalsIgnoreCase(req.getMethod().toString())) {
			// Bad request - return simple error page
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			return;
		}
		
		String path = req.getPath().substring(1);
		String[] segments = path.split("/");
		String streamName;
		if (segments.length < 2) {		
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);		
			return;
		}
		streamName = segments[1];		
		
		// play security
		IStreamSecurityService security = (IStreamSecurityService) ScopeUtils.getScopeService(scope, IStreamSecurityService.class);
		if (security != null) {
			Set<IStreamPlaybackSecurity> handlers = security.getStreamPlaybackSecurity();
			for (IStreamPlaybackSecurity handler : handlers) {
				if (!handler.isPlaybackAllowed(scope, streamName, 0, 0, false)) {
					sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
					return;
				}
			}
		}
		
		HTTPConnectionConsumer consumer = new HTTPConnectionConsumer(conn);		
		
		conn.getHttpSession().getConfig().setReaderIdleTime(0);
		conn.getHttpSession().getConfig().setWriterIdleTime(0);
		conn.getHttpSession().getConfig().setIdleTime(IdleStatus.WRITER_IDLE, Configuration.HTTP_IDLE);
		
		consumer.getConnection().connect(scope);
		CustomSingleItemSubStream stream = new CustomSingleItemSubStream(scope, consumer);
		SimplePlayItem playItem = SimplePlayItem.build(streamName, -2000, -1);
		stream.setPlayItem(playItem);
		stream.start();
		
		conn.setAttribute("consumer", consumer);
		conn.setAttribute("stream", stream);
		
		setHeader(resp);
		conn.write(resp);
		
		try {
			stream.play();
		} catch (IOException e) {
			log.info("http play faile {}", e.getMessage());
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			stream.stop();
			return;
		}
		
		if (stream.isFailure()) {
			log.info("stream {} http play faile", streamName);
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			stream.stop();
			return;
		}
	}
}
