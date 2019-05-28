package com.sms.server.net.rtsp.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.sms.server.net.http.codec.DecodeState;
import com.sms.server.net.http.codec.HTTPMessageDecoder;
import com.sms.server.net.http.message.HTTPMessage;
import com.sms.server.net.rtsp.RTSPMinaConnection;
import com.sms.server.net.rtsp.message.RTSPChannelData;
import com.sms.server.net.rtsp.message.RTSPHeaders;

/**
 * RTSP Message Decoder
 * @author pengliren
 *
 */
public abstract class RTSPMessageDecoder extends HTTPMessageDecoder {

	@Override
    protected boolean isContentAlwaysEmpty(HTTPMessage msg) {
        // Unlike HTTP, RTSP always assumes zero-length body if Content-Length
        // header is absent.
        boolean empty = super.isContentAlwaysEmpty(msg);
        if (empty) {
            return true;
        }
        if (!msg.containsHeader(RTSPHeaders.Names.CONTENT_LENGTH)) {
            return true;
        }
        return empty;
    }
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {	
		
		//get the connection from the session
		RTSPMinaConnection conn = (RTSPMinaConnection) session.getAttribute(RTSPMinaConnection.RTSP_CONNECTION_KEY);
		conn.getWriteLock().lock();
		try {			
			boolean ret = false;
			int pos = 0;
			while (in.remaining() > 4) {
				if (in.get() == 36) {					
					byte channel = in.get();// channel
					int len = (int) (in.getShort() & 0xFFFF);
					if (len > in.remaining()) {
						pos = in.position();
						in.position(pos - 4);
						ret = false;
						break;
					} else {						
						byte[] temp = new byte[len];
						in.get(temp);
						IoBuffer data = IoBuffer.wrap(temp);
						RTSPChannelData channelData = new RTSPChannelData(channel, data);
						out.write(channelData);
						ret = true;
					}
				} else {					
					pos = in.position();
					in.position(pos - 1);
					DecodeState obj = decodeBuffer(in);
					if(obj.getState() == DecodeState.ENOUGH) {
						ret = true;
						if(obj.getObject() != null) out.write(obj.getObject());
					} else {
						ret = false;
						break;
					}
				}					
			}
			return ret;
		} catch (Exception e) {
			throw new ProtocolCodecException(e);
		} finally {
			conn.getWriteLock().unlock();
		}
	}
}
