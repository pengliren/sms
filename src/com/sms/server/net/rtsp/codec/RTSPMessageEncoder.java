package com.sms.server.net.rtsp.codec;

import java.nio.charset.CharacterCodingException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.sms.io.object.UnsignedShort;
import com.sms.server.api.IConnection;
import com.sms.server.api.SMS;
import com.sms.server.net.http.codec.HTTPMessageEncoder;
import com.sms.server.net.http.message.HTTPMessage;
import com.sms.server.net.rtp.RTPPacket;
import com.sms.server.net.rtsp.RTSPMinaConnection;

/**
 * RTSP Message Encoder
 * @author pengliren
 *
 */
public abstract class RTSPMessageEncoder extends HTTPMessageEncoder {

	public RTSPMessageEncoder() throws CharacterCodingException {
		super();
	}
	
	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws ProtocolCodecException {
		
		RTSPMinaConnection conn = (RTSPMinaConnection) session.getAttribute(RTSPMinaConnection.RTSP_CONNECTION_KEY);
		final IConnection prevConn = SMS.getConnectionLocal();
		conn.getWriteLock().lock();
		try {
			if(message instanceof HTTPMessage) {
				IoBuffer buf;
				try {
					buf = encodeBuffer(message);
					if (buf != null) {					
						out.write(buf);
						out.mergeAll();
						out.flush();
					} 
				} catch (Exception e) {
					throw new ProtocolCodecException(e);
				}
			} else if(message instanceof RTPPacket) {
				RTPPacket rtpPacket = (RTPPacket) message;	
				IoBuffer temp = rtpPacket.toByteBuffer();
				IoBuffer buf = IoBuffer.allocate(temp.limit() + 4);
				buf.put((byte) '$');
				// channel
				buf.put((byte) rtpPacket.getChannel());
				// rtp size
				buf.put(new UnsignedShort(temp.limit()).getBytes());
				buf.put(temp);
				buf.flip();
				out.write(buf);
				out.mergeAll();
				out.flush();
			}
		} finally {
			conn.getWriteLock().unlock();
			SMS.setConnectionLocal(prevConn);
		}
	}
}
