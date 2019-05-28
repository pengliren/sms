package com.sms.server.net.http.codec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;

import com.sms.server.net.http.message.HTTPChunk;
import com.sms.server.net.http.message.HTTPChunkTrailer;
import com.sms.server.net.http.message.HTTPMessage;
import com.sms.server.net.http.message.HTTPHeaders.Names;
import com.sms.server.net.http.message.HTTPHeaders.Values;
import com.sms.server.util.CharsetUtil;

/**
 * HTTP Message Encoder
 * @author pengliren
 *
 */
public abstract class HTTPMessageEncoder extends ProtocolEncoderAdapter {

	private static final IoBuffer LAST_CHUNK = IoBuffer.allocate(10).setAutoExpand(true);
	
	private volatile boolean chunked;
	
	public HTTPMessageEncoder() throws CharacterCodingException {		
		LAST_CHUNK.putString("0\r\n\r\n", CharsetUtil.US_ASCII.newEncoder());
	}
	
	protected IoBuffer encodeBuffer(Object msg) throws Exception {
		
		if (msg instanceof HTTPMessage) {
            HTTPMessage m = (HTTPMessage) msg;
            boolean chunked;
            if (m.isChunked()) {
                // check if the Transfer-Encoding is set to chunked already.
                // if not add the header to the message
                if (!HTTPCodecUtil.isTransferEncodingChunked(m)) {
                    m.addHeader(Names.TRANSFER_ENCODING, Values.CHUNKED);
                }
                chunked = this.chunked = true;
            } else {
                chunked = this.chunked = HTTPCodecUtil.isTransferEncodingChunked(m);
            }
            IoBuffer header = IoBuffer.allocate(2048).setAutoExpand(true);
            encodeInitialLine(header, m);
            encodeHeaders(header, m);
            header.put(HTTPCodecUtil.CR);
            header.put(HTTPCodecUtil.LF);

            IoBuffer content = m.getContent();
            if (content == null || content.remaining() <= 0) {
                return header.flip(); // no content
            } else if (chunked) {
                throw new IllegalArgumentException(
                        "HttpMessage.content must be empty " +
                        "if Transfer-Encoding is chunked.");
            } else {
                return header.put(content).flip();
            }
        }

        if (msg instanceof HTTPChunk) {
            HTTPChunk chunk = (HTTPChunk) msg;
            if (chunked) {
                if (chunk.isLast()) {
                    chunked = false;
                    if (chunk instanceof HTTPChunkTrailer) {
                        IoBuffer trailer = IoBuffer.allocate(2048).setAutoExpand(true);
                        trailer.put((byte) '0');
                        trailer.put(HTTPCodecUtil.CR);
                        trailer.put(HTTPCodecUtil.LF);
                        encodeTrailingHeaders(trailer, (HTTPChunkTrailer) chunk);
                        trailer.put(HTTPCodecUtil.CR);
                        trailer.put(HTTPCodecUtil.LF);
                        return trailer;
                    } else {
                        return LAST_CHUNK.duplicate();
                    }
                } else {                	
                    IoBuffer content = chunk.getContent();
                    content.flip();
                    int contentLength = content.remaining();
                    IoBuffer temp = IoBuffer.allocate(2048).setAutoExpand(true);
                    temp.putString(Integer.toHexString(contentLength), CharsetUtil.US_ASCII.newEncoder());
                    temp.put(HTTPCodecUtil.CRLF);
                    temp.put(content);
                    temp.put(HTTPCodecUtil.CRLF);
                    temp.flip();
                    return temp;
                }
            } else {
                if (chunk.isLast()) {
                    return null;
                } else {
                    return chunk.getContent();
                }
            }

        }

        // Unknown message type.
        return null;
	}
	
	private void encodeHeaders(IoBuffer buf, HTTPMessage message) {
        try {
            for (Map.Entry<String, String> h: message.getHeaders()) {
                encodeHeader(buf, h.getKey(), h.getValue());
            }
        } catch (UnsupportedEncodingException e) {
            throw (Error) new Error().initCause(e);
        }
    }

    private void encodeTrailingHeaders(IoBuffer buf, HTTPChunkTrailer trailer) {
        try {
            for (Map.Entry<String, String> h: trailer.getHeaders()) {
                encodeHeader(buf, h.getKey(), h.getValue());
            }
        } catch (UnsupportedEncodingException e) {
            throw (Error) new Error().initCause(e);
        }
    }

    private void encodeHeader(IoBuffer buf, String header, String value)
            throws UnsupportedEncodingException {
        buf.put(header.getBytes("ASCII"));
        buf.put(HTTPCodecUtil.COLON);
        buf.put(HTTPCodecUtil.SP);
        buf.put(value.getBytes("ASCII"));
        buf.put(HTTPCodecUtil.CR);
        buf.put(HTTPCodecUtil.LF);
    }
    
	protected abstract void encodeInitialLine(IoBuffer buf, HTTPMessage message) throws Exception;
}
