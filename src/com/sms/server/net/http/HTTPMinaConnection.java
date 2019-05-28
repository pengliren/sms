package com.sms.server.net.http;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import com.sms.server.BaseConnection;
import com.sms.server.api.stream.IClientBroadcastStream;
import com.sms.server.api.stream.IClientStream;
import com.sms.server.api.stream.IPlaylistSubscriberStream;
import com.sms.server.api.stream.ISingleItemSubscriberStream;
import com.sms.server.api.stream.IStreamCapableConnection;

/**
 * HTTP Mina Connection
 * @author pengliren
 *
 */
public class HTTPMinaConnection extends BaseConnection implements IStreamCapableConnection {

	public static final String HTTP_CONNECTION_KEY = "http.conn";
	
	private boolean isClosed = false;
	
	private IoSession httpSession;
	
	private AtomicInteger pendings = new AtomicInteger();
	
	private IHTTPApplicationAdapter applicationAdapter;
	
	public HTTPMinaConnection(IoSession session) {

		httpSession = session;		
	}
	
	public void close() {

		if (isClosed == false) {
			isClosed = true;
		} else {
			return;
		}
		
		if(applicationAdapter != null) applicationAdapter.onConnectionClose(this);
	}
	
	public WriteFuture write(Object out) {
		
		pendings.incrementAndGet();
		return httpSession.write(out);		
	}
	
	public void messageSent(Object message) {

		if (message instanceof IoBuffer) {
			pendings.decrementAndGet();
		}
	}
	
	@Override
	public Encoding getEncoding() {
		return null;
	}

	@Override
	public void ping() {
		
	}

	@Override
	public int getLastPingTime() {
		return 0;
	}

	@Override
	public void setBandwidth(int mbits) {
		
	}

	@Override
	public int reserveStreamId() {
		return 0;
	}

	@Override
	public int reserveStreamId(int id) {
		return 0;
	}

	@Override
	public void unreserveStreamId(int streamId) {
		
	}

	@Override
	public void deleteStreamById(int streamId) {
		
	}

	@Override
	public IClientStream getStreamById(int streamId) {
		return null;
	}

	@Override
	public ISingleItemSubscriberStream newSingleItemSubscriberStream(int streamId) {
		return null;
	}

	@Override
	public IPlaylistSubscriberStream newPlaylistSubscriberStream(int streamId) {
		return null;
	}

	@Override
	public IClientBroadcastStream newBroadcastStream(int streamId) {
		return null;
	}

	@Override
	public long getReadBytes() {
		return httpSession.getReadBytes();
	}

	@Override
	public long getWrittenBytes() {
		return httpSession.getWrittenBytes();
	}

	@Override
	public long getPendingMessages() {
		
		return pendings.longValue();
	}
	
	public boolean isClosing() {

		return httpSession.isClosing();
	}
	
	public IoSession getHttpSession() {
		
		return httpSession;
	}

	public IHTTPApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	public void setApplicationAdapter(IHTTPApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}
}
