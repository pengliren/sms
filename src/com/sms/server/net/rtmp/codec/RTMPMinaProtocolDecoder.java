package com.sms.server.net.rtmp.codec;

import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.sms.server.api.SMS;
import com.sms.server.net.rtmp.RTMPConnection;
import com.sms.server.net.rtmp.message.Constants;
import com.sms.server.net.rtmp.protocol.ProtocolState;

/**
 * RTMP protocol decoder.
 */
public class RTMPMinaProtocolDecoder extends ProtocolDecoderAdapter {

	private RTMPProtocolDecoder decoder = new RTMPProtocolDecoder();
	
	/** {@inheritDoc} */
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws ProtocolCodecException {
    	//get our state
		final ProtocolState state = (ProtocolState) session.getAttribute(ProtocolState.SESSION_KEY);
		//get the connection from the session
		RTMPConnection conn = (RTMPConnection) session.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY);
		conn.getWriteLock().lock();
		try {
			//create a buffer and store it on the session
			IoBuffer buf = (IoBuffer) session.getAttribute("buffer");
			if (buf == null) {
				buf = IoBuffer.allocate(Constants.HANDSHAKE_SIZE);
				buf.setAutoExpand(true);
				session.setAttribute("buffer", buf);
			}
			buf.put(in);
			buf.flip();
			
			// look for the connection local, if not set then get from the session and set it to prevent any
			// decode failures
			if (SMS.getConnectionLocal() == null) {
				SMS.setConnectionLocal(conn);
			}
			//construct any objects from the decoded bugger
			List<?> objects = decoder.decodeBuffer(state, buf);
			if (objects != null) {
				for (Object object : objects) {
					out.write(object);
				}
			}
		} finally {
			conn.getWriteLock().unlock();
		}
	} 
    
	public RTMPProtocolDecoder getDecoder() {
		return decoder;
	}

}
