package com.sms.server.net.rtmp;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IConnection;
import com.sms.server.api.SMS;
import com.sms.server.net.rtmp.codec.RTMP;
import com.sms.server.net.rtmp.codec.RTMPMinaCodecFactory;
import com.sms.server.net.rtmp.message.Constants;
import com.sms.server.net.rtmp.protocol.ProtocolState;

public class RTMPMinaIoHandler extends IoHandlerAdapter {

	private static Logger log = LoggerFactory.getLogger(RTMPMinaIoHandler.class);

	/**
	 * RTMP events handler
	 */
	protected IRTMPHandler handler;

	/**
	 * Mode
	 */
	protected boolean mode = RTMP.MODE_SERVER;
	
	/**
	 * RTMP protocol codec factory
	 */
	protected ProtocolCodecFactory codecFactory;

	protected IRTMPConnManager rtmpConnManager;
	
	public RTMPMinaIoHandler() {
		
		handler = new RTMPHandler();
		codecFactory = new RTMPMinaCodecFactory();
		rtmpConnManager = new RTMPConnManager();
	}
	
	/** {@inheritDoc} */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		log.debug("Session created");
		// moved protocol state from connection object to RTMP object
		RTMP rtmp = new RTMP(mode);
		session.setAttribute(ProtocolState.SESSION_KEY, rtmp);
		//add rtmp filter
		session.getFilterChain().addFirst("rtmpFilter", new RTMPIoFilter());		
		//add protocol filter next
		session.getFilterChain().addLast("protocolFilter", new ProtocolCodecFilter(codecFactory));
		if (log.isTraceEnabled()) {
			session.getFilterChain().addLast("logger", new LoggingFilter());
		}
		//create a connection
		RTMPMinaConnection conn = createRTMPMinaConnection();
		conn.setIoSession(session);
		conn.setState(rtmp);
		//add the connection
		session.setAttribute(RTMPConnection.RTMP_CONNECTION_KEY, conn);
		//create inbound or outbound handshaker
		if (rtmp.getMode() == RTMP.MODE_CLIENT) {
			// create an outbound handshake
			OutboundHandshake outgoingHandshake = new OutboundHandshake();
			//if handler is rtmpe client set encryption on the protocol state
			//if (handler instanceof RTMPEClient) {
				//rtmp.setEncrypted(true);
				//set the handshake type to encrypted as well
				//outgoingHandshake.setHandshakeType(RTMPConnection.RTMP_ENCRYPTED);
			//}
			//add the handshake
			session.setAttribute(RTMPConnection.RTMP_HANDSHAKE, outgoingHandshake);
			// set a reference to the connection on the client
			if (handler instanceof BaseRTMPClientHandler) {
				((BaseRTMPClientHandler) handler).setConnection((RTMPConnection) conn);
			}
		} else {
			//add the handshake
			session.setAttribute(RTMPConnection.RTMP_HANDSHAKE, new InboundHandshake());			
		}
	}

	/** {@inheritDoc} */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		log.debug("Session opened");		
		super.sessionOpened(session);
		// get protocol state
		RTMP rtmp = (RTMP) session.getAttribute(ProtocolState.SESSION_KEY);
		if (rtmp.getMode() == RTMP.MODE_CLIENT) {
			log.debug("Handshake - client phase 1");
			//get the handshake from the session
			RTMPHandshake handshake = (RTMPHandshake) session.getAttribute(RTMPConnection.RTMP_HANDSHAKE);
			session.write(handshake.doHandshake(null));
		} else {
			handler.connectionOpened((RTMPMinaConnection) session.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY), rtmp);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		log.debug("Session closed");	
		RTMP rtmp = (RTMP) session.removeAttribute(ProtocolState.SESSION_KEY);
		log.debug("RTMP state: {}", rtmp);	
		RTMPMinaConnection conn = (RTMPMinaConnection) session.removeAttribute(RTMPConnection.RTMP_CONNECTION_KEY);
		if(conn != null && rtmp != null) {
			// fire-off closed
			handler.connectionClosed(conn, rtmp);
			// remove the handshake if not already done
			if (session.containsAttribute(RTMPConnection.RTMP_HANDSHAKE)) {
	    		session.removeAttribute(RTMPConnection.RTMP_HANDSHAKE);
			}
			// remove ciphers
			if (session.containsAttribute(RTMPConnection.RTMPE_CIPHER_IN)) {
				session.removeAttribute(RTMPConnection.RTMPE_CIPHER_IN);
				session.removeAttribute(RTMPConnection.RTMPE_CIPHER_OUT);
			}
			rtmpConnManager.removeConnection(conn.getId());
		}		
	}

	/**
	 * Handle raw buffer receiving event.
	 * 
	 * @param in
	 *            Data buffer
	 * @param session
	 *            I/O session, that is, connection between two endpoints
	 */
	protected void rawBufferRecieved(IoBuffer in, IoSession session) {
		log.debug("rawBufferRecieved: {}", in);
		final RTMP rtmp = (RTMP) session.getAttribute(ProtocolState.SESSION_KEY);
		log.debug("state: {}", rtmp);
		final RTMPMinaConnection conn = (RTMPMinaConnection) session.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY);
		RTMPHandshake handshake = (RTMPHandshake) session.getAttribute(RTMPConnection.RTMP_HANDSHAKE);
		if (handshake != null) {
			IoBuffer out = null;
			conn.getWriteLock().lock();
			try {
				if (rtmp.getMode() == RTMP.MODE_SERVER) {
					if (rtmp.getState() != RTMP.STATE_HANDSHAKE) {
						log.warn("Raw buffer after handshake, something odd going on");
					}
					log.debug("Handshake - server phase 1 - size: {}", in.remaining());
				} else {
					log.debug("Handshake - client phase 2 - size: {}", in.remaining());
				}
				out = handshake.doHandshake(in);
			} finally {
				conn.getWriteLock().unlock();
				if (out != null) {
					log.debug("Output: {}", out);
					session.write(out);
					//if we are connected and doing encryption, add the ciphers
					if (rtmp.getState() == RTMP.STATE_CONNECTED) {
						// remove handshake from session now that we are connected
						//session.removeAttribute(RTMPConnection.RTMP_HANDSHAKE);
		    			// if we are using encryption then put the ciphers in the session
		        		if (handshake.getHandshakeType() == RTMPConnection.RTMP_ENCRYPTED) {
		        			log.debug("Adding ciphers to the session");
		        			session.setAttribute(RTMPConnection.RTMPE_CIPHER_IN, handshake.getCipherIn());
		        			session.setAttribute(RTMPConnection.RTMPE_CIPHER_OUT, handshake.getCipherOut());
		        		}	
					}
				}
			}		
		} else {
			log.warn("Handshake was not found for this connection: {}", conn);
			log.debug("RTMP state: {} Session: {}", rtmp, session);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		log.debug("messageReceived");
		if (message instanceof IoBuffer) {
			rawBufferRecieved((IoBuffer) message, session);
		} else {
			log.debug("Setting connection local");
			IConnection connection = (IConnection) session.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY); 
			if(connection != null) {
				SMS.setConnectionLocal(connection);			
				handler.messageReceived(message, session);
				log.debug("Removing connection local");
				SMS.setConnectionLocal(null);
			} else {
				log.warn("Connection was not found for {}", session.getId());
				forceClose(session);
			}			
		}
	}

	/** {@inheritDoc} */
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		log.debug("messageSent");
		final RTMPMinaConnection conn = (RTMPMinaConnection) session.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY);
		handler.messageSent(conn, message);
		if (mode == RTMP.MODE_CLIENT) {
			if (message instanceof IoBuffer) {
				if (((IoBuffer) message).limit() == Constants.HANDSHAKE_SIZE) {
					RTMP rtmp = (RTMP) session.getAttribute(ProtocolState.SESSION_KEY);
					handler.connectionOpened(conn, rtmp);
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		log.warn("Exception caught {}", cause.getMessage());
		forceClose(session);
	}
	
	/**
	 * Force the NioSession to be released and cleaned up.
	 * 
	 * @param session
	 */
	private void forceClose(final IoSession session) {
		log.warn("Force close - session: {}", session.getId());
		if (session.containsAttribute("FORCED_CLOSE")) {
			log.warn("Close already forced on this session: {}", session.getId());
		} else {
			// set flag
			session.setAttribute("FORCED_CLOSE", Boolean.TRUE);
			// clean up			
			log.debug("Session closing: {}", session.isClosing());
			session.suspendRead();
			final WriteRequestQueue writeQueue = session.getWriteRequestQueue();
			if (writeQueue != null && !writeQueue.isEmpty(session)) {
				log.debug("Clearing write queue");
				writeQueue.clear(session);
			}
			// force close the session
			final CloseFuture future = session.close(false);
			IoFutureListener<CloseFuture> listener = new IoFutureListener<CloseFuture>() {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				public void operationComplete(CloseFuture future) {
					// now connection should be closed
					log.debug("Close operation completed: {}", future.isClosed());
					future.removeListener(this);
					for (Object key : session.getAttributeKeys()) {
						Object obj = session.getAttribute(key);
						if (obj instanceof IoProcessor) {
							log.debug("Flushing session in processor");
							((IoProcessor) obj).flush(session);
							log.debug("Removing session from processor");
							((IoProcessor) obj).remove(session);
						} else if (obj instanceof IoBuffer) {
							log.debug("Clearing session buffer");
							((IoBuffer) obj).clear();
							((IoBuffer) obj).free();							
						}
					}
				}
			};
			future.addListener(listener);
		}
	}
	
	/**
	 * Setter for handler.
	 * 
	 * @param handler RTMP events handler
	 */
	public void setHandler(IRTMPHandler handler) {
		this.handler = handler;
	}

	/**
	 * Setter for mode.
	 * 
	 * @param mode <code>true</code> if handler should work in server mode,
	 *            <code>false</code> otherwise
	 */
	public void setMode(boolean mode) {
		this.mode = mode;
	}

	/**
	 * Setter for codec factory.
	 * 
	 * @param codecFactory RTMP protocol codec factory
	 */
	public void setCodecFactory(ProtocolCodecFactory codecFactory) {
		this.codecFactory = codecFactory;
	}

	public void setRtmpConnManager(IRTMPConnManager rtmpConnManager) {
		this.rtmpConnManager = rtmpConnManager;
	}

	protected IRTMPConnManager getRtmpConnManager() {
		return rtmpConnManager;
	}
	
	protected RTMPMinaConnection createRTMPMinaConnection() {
		return (RTMPMinaConnection) rtmpConnManager.createConnection(RTMPMinaConnection.class);
	}
}
