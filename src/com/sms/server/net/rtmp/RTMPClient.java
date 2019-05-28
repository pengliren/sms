package com.sms.server.net.rtmp;

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.net.rtmp.codec.RTMP;


public class RTMPClient extends BaseRTMPClientHandler {

	private static final Logger logger = LoggerFactory.getLogger(RTMPClient.class);
	
	protected static final int CONNECTOR_WORKER_TIMEOUT = 7000; // seconds
	
	// I/O handler
	private final RTMPMinaIoHandler ioHandler;
	
	// Socket connector, disposed on disconnect
	protected SocketConnector socketConnector;
	
	// 
	protected ConnectFuture future;

	/** Constructs a new RTMPClient. */
    public RTMPClient() {
		ioHandler = new RTMPMinaIoHandler();
		ioHandler.setCodecFactory(getCodecFactory());
		ioHandler.setMode(RTMP.MODE_CLIENT);
		ioHandler.setHandler(this);
		ioHandler.setRtmpConnManager(RTMPClientConnManager.getInstance());
	}

	public Map<String, Object> makeDefaultConnectionParams(String server, int port, String application) {
		Map<String, Object> params = super.makeDefaultConnectionParams(server, port, application);
		if (!params.containsKey("tcUrl")) {
			params.put("tcUrl", String.format("rtmp://%s:%s/%s", server, port, application));
		}
		return params;
	}
	
	@Override
	protected void startConnector(String server, int port) {
		socketConnector = new NioSocketConnector();		
		socketConnector.setHandler(ioHandler);
		future = socketConnector.connect(new InetSocketAddress(server, port));
		future.addListener(new IoFutureListener<ConnectFuture>() {
			public void operationComplete(ConnectFuture future) {
				try {
					// will throw RuntimeException after connection error
					future.getSession();
				} catch (Throwable e) {
					socketConnector.dispose(false);
					//if there isn't an ClientExceptionHandler set, a 
					//RuntimeException may be thrown in handleException
					handleException(e);
				}
			}
		});
		// Now wait for the close to be completed
		future.awaitUninterruptibly(CONNECTOR_WORKER_TIMEOUT);
	}
	
	@Override
	public void disconnect() {
		if (future != null) {
			try {
				// close requesting that the pending messages are sent before the session is closed
				future.getSession().close(false);
				// now wait for the close to be completed
				future.awaitUninterruptibly(CONNECTOR_WORKER_TIMEOUT);
			} catch (Exception e) {
				logger.warn("Exception during disconnect", e);
			} finally {
				// We can now dispose the connector
				socketConnector.dispose(false);
			}
		}
		super.disconnect();
	}
}
