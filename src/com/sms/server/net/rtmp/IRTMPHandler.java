package com.sms.server.net.rtmp;

import org.apache.mina.core.session.IoSession;

import com.sms.server.net.rtmp.codec.RTMP;

/**
 * @ClassName: IRTMPHandler
 * @Description:  RTMP events handler
 * @author pengliren
 *
 */
public interface IRTMPHandler {

	/**
     * Connection open event
     * 
     * @param conn          Connection
     * @param state         RTMP state
     */
	public void connectionOpened(RTMPConnection conn, RTMP state);

    /**
     * Message received
     * 
     * @param message       Message
     * @param session       Connected session
     * @throws Exception    Exception
     */
	public void messageReceived(Object message, IoSession session) throws Exception;

    /**
     * Message sent
     * @param conn          Connection
     * @param message       Message
     */
	public void messageSent(RTMPConnection conn, Object message);

    /**
     * Connection closed
     * @param conn          Connection
     * @param state         RTMP state
     */
	public void connectionClosed(RTMPConnection conn, RTMP state);
}
