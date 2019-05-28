package com.sms.server.net.http.codec;

import static com.sms.server.net.rtsp.message.RTSPHeaders.Names.CONTENT_TYPE;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;

import com.sms.server.net.http.message.DefaultHttpChunk;
import com.sms.server.net.http.message.DefaultHttpChunkTrailer;
import com.sms.server.net.http.message.HTTPChunk;
import com.sms.server.net.http.message.HTTPChunkTrailer;
import com.sms.server.net.http.message.HTTPHeaders;
import com.sms.server.net.http.message.HTTPMessage;
import com.sms.server.net.http.message.HTTPResponse;

/**
 * HTTP Message Decoder
 * @author pengliren
 *
 */
public abstract class HTTPMessageDecoder extends CumulativeProtocolDecoder {

	private HTTPMessage message;
    private IoBuffer content;
    private long chunkSize;
    private int headerSize;
    private long maxChunkSize = 4096;
    private State state;
    
    private final StringBuilder sb = new StringBuilder(128);
    
	/**
     * The internal state of {@link HTTPMessageDecoder}.
     * <em>Internal use only</em>.
     */
    protected enum State {
        SKIP_CONTROL_CHARS,
        READ_INITIAL,
        READ_HEADER,
        READ_VARIABLE_LENGTH_CONTENT,
        READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS,
        READ_FIXED_LENGTH_CONTENT,
        READ_FIXED_LENGTH_CONTENT_AS_CHUNKS,
        READ_CHUNK_SIZE,
        READ_CHUNKED_CONTENT,
        READ_CHUNKED_CONTENT_AS_CHUNKS,
        READ_CHUNK_DELIMITER,
        READ_CHUNK_FOOTER
    }
    
    public HTTPMessageDecoder() {
	
    	state = State.SKIP_CONTROL_CHARS;
	}
    
    public DecodeState decodeBuffer(IoBuffer buffer) throws Exception {
    	
    	DecodeState decodeState = new DecodeState(); 
    	switch (state) {
	    	case SKIP_CONTROL_CHARS: {	            
                skipControlCharacters(buffer);
                state = State.READ_INITIAL;	            
	        }
	    	case READ_INITIAL: {
	    		String line = readLine(buffer);
	    		if(line == null) {
	    			decodeState.setState(DecodeState.NOT_ENOUGH);
	    			return decodeState;
	    		}
	    		String[] initialLine = splitInitialLine(line);
	    		if (initialLine.length < 3) {
	                // Invalid initial line - ignore.
	    			state = State.SKIP_CONTROL_CHARS;
	                return decodeState;
	            }
	    		message = createMessage(initialLine);
	    		state = State.READ_HEADER;
	    	}
	    	case READ_HEADER: {
	    		State nextState = readHeaders(buffer);
	    		if(nextState == null) {
	    			decodeState.setState(DecodeState.NOT_ENOUGH);
	    			return decodeState;
	    		}
	    		state = nextState;
	    		if (nextState == State.READ_CHUNK_SIZE) {
	    			 // Chunked encoding
	                message.setChunked(true);
	                // Generate HttpMessage first.  HttpChunks will follow.
	                decodeState.setObject(message);
	                return decodeState;
	    		} else if (nextState == State.SKIP_CONTROL_CHARS) {
	    			// No content is expected.
	                // Remove the headers which are not supposed to be present not
	                // to confuse subsequent handlers.
	                message.removeHeader(HTTPHeaders.Names.TRANSFER_ENCODING);
	                decodeState.setObject(message);
	                return decodeState;
	    		} else {
	    			long contentLength = HTTPHeaders.getContentLength(message, -1);
	                if (contentLength == 0 || contentLength == -1) {
	                    content = null;
	                    return reset(decodeState);
	                }
	                
	                switch (nextState) {
	                case READ_FIXED_LENGTH_CONTENT:
	                    if (HTTPHeaders.is100ContinueExpected(message)) {
	                        // Generate HttpMessage first.  HttpChunks will follow.
	                        state = State.READ_FIXED_LENGTH_CONTENT_AS_CHUNKS;
	                        message.setChunked(true);
	                        // chunkSize will be decreased as the READ_FIXED_LENGTH_CONTENT_AS_CHUNKS
	                        // state reads data chunk by chunk.
	                        chunkSize = HTTPHeaders.getContentLength(message, -1);
	    	                decodeState.setObject(message);
	                        return decodeState;
	                    }
	                    break;
	                case READ_VARIABLE_LENGTH_CONTENT:
	                    if (HTTPHeaders.is100ContinueExpected(message)) {
	                        // Generate HttpMessage first.  HttpChunks will follow.
	                        state = State.READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS;
	                        message.setChunked(true);
	    	                decodeState.setObject(message);
	                        return decodeState;
	                    }
	                    break;
	                default:
	                    throw new IllegalStateException("Unexpected state: " + nextState);
	                }	                
	    		}
	    		// We return null here, this forces decode to be called again where we will decode the content
	            return decodeState;
	    	}
	    	case READ_VARIABLE_LENGTH_CONTENT: {
	    		if (content == null) {
	                content = IoBuffer.allocate(2048).setAutoExpand(true);
	            }
	            //this will cause a replay error until the channel is closed where this will read what's left in the buffer
	    		content.put(buffer);	            
	            return reset(decodeState);
	    	}
	    	case READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS: {
	    		// Keep reading data as a chunk until the end of connection is reached.
	    		byte[] chunkData = new byte[(int)chunkSize];
	    		buffer.get(chunkData);
	            HTTPChunk chunk = new DefaultHttpChunk(IoBuffer.wrap(chunkData));
	            if (buffer.remaining() == 0) {
	                // Reached to the end of the connection.
	                reset(decodeState);
	                if (!chunk.isLast()) {
	                    // Append the last chunk.
	                	decodeState.setObject(new Object[] { chunk, HTTPChunk.LAST_CHUNK });
	                    return decodeState;
	                }
	            }
	            decodeState.setObject(chunk);
	            return decodeState;
	    	}
	    	case READ_FIXED_LENGTH_CONTENT: {
	            //we have a content-length so we just read the correct number of bytes
	    		AtomicBoolean reset = new AtomicBoolean(true);
	            if(!readFixedLengthContent(buffer, reset)) {
	            	decodeState.setState(DecodeState.NOT_ENOUGH);
	    			return decodeState;
	            }
	            if(reset.get()) return reset(decodeState);
	            else return noreset(decodeState);
	        }
	    	case READ_FIXED_LENGTH_CONTENT_AS_CHUNKS: {
	            long chunkSize = this.chunkSize;
	            HTTPChunk chunk;	            
                assert chunkSize <= Integer.MAX_VALUE;
                byte[] chunkData = new byte[(int) chunkSize];
                buffer.get(chunkData);
                chunk = new DefaultHttpChunk(IoBuffer.wrap(chunkData));
                chunkSize = 0;
	            this.chunkSize = chunkSize;
	            if (chunkSize == 0) {
	                // Read all content.
	                reset(decodeState);
	                if (!chunk.isLast()) {
	                    // Append the last chunk.
	                	decodeState.setObject(new Object[] { chunk, HTTPChunk.LAST_CHUNK });
	                    return decodeState;
	                }
	            }
	            decodeState.setObject(chunk);
	            return decodeState;
	        }
	    	case READ_CHUNK_SIZE: {
	            String line = readLine(buffer);
	            if(line == null) {
	            	decodeState.setState(DecodeState.NOT_ENOUGH);
	            	return decodeState;
	            }
	            int chunkSize = getChunkSize(line);
	            this.chunkSize = chunkSize;
	            if (chunkSize == 0) {
	                state = State.READ_CHUNK_FOOTER;
	            	return decodeState;
	            } else if (chunkSize > maxChunkSize) {
	                // A chunk is too large. Split them into multiple chunks again.
	            	state = State.READ_CHUNKED_CONTENT_AS_CHUNKS;
	            } else {
	            	state = State.READ_CHUNKED_CONTENT;
	            }
	        }
	    	case READ_CHUNKED_CONTENT: {
	            assert chunkSize <= Integer.MAX_VALUE;
	            byte[] chunkData = new byte[(int) chunkSize];
	            if(buffer.remaining() < chunkSize) {
	            	decodeState.setState(DecodeState.NOT_ENOUGH);
	            	return decodeState;
	            }
                buffer.get(chunkData);
	            HTTPChunk chunk = new DefaultHttpChunk(IoBuffer.wrap(chunkData));
	            state = State.READ_CHUNK_DELIMITER;
	            decodeState.setObject(chunk);
	            return decodeState;
	        }
	    	case READ_CHUNKED_CONTENT_AS_CHUNKS: {
	            long chunkSize = this.chunkSize;
	            HTTPChunk chunk;
	            if (chunkSize > maxChunkSize) {
	            	byte[] chunkData = new byte[(int) maxChunkSize];
	            	if(buffer.remaining() < maxChunkSize) {
	            		decodeState.setState(DecodeState.NOT_ENOUGH);
		            	return decodeState;
	            	}
	                buffer.get(chunkData);
	                chunk = new DefaultHttpChunk(IoBuffer.wrap(chunkData));
	                chunkSize -= maxChunkSize;
	            } else {
	                assert chunkSize <= Integer.MAX_VALUE;
	                byte[] chunkData = new byte[(int) chunkSize];
	                if(buffer.remaining() < chunkSize) {
	                	decodeState.setState(DecodeState.NOT_ENOUGH);
		            	return decodeState;
	                }
	                buffer.get(chunkData);
	                chunk = new DefaultHttpChunk(IoBuffer.wrap(chunkData));
	                chunkSize = 0;
	            }
	            this.chunkSize = chunkSize;

	            if (chunkSize == 0) {
	                // Read all content.
	            	state = State.READ_CHUNK_DELIMITER;
	            }

	            if (!chunk.isLast()) {
	            	decodeState.setObject(chunk);
	            	return decodeState;
	            }
	        }
	    	case READ_CHUNK_DELIMITER: {
	            for (;;) {
	            	if(buffer.remaining() < 2) {
	            		decodeState.setState(DecodeState.NOT_ENOUGH);
		            	return decodeState;
	            	}
	                byte next = buffer.get();
	                if (next == HTTPCodecUtil.CR) {
	                    if (buffer.get() == HTTPCodecUtil.LF) {
	                    	state = State.READ_CHUNK_SIZE;
			            	return decodeState;
	                    }
	                } else if (next == HTTPCodecUtil.LF) {
	                	state = State.READ_CHUNK_SIZE;
		            	return decodeState;
	                }
	            }
	        }
	    	case READ_CHUNK_FOOTER: {
	            HTTPChunkTrailer trailer = readTrailingHeaders(buffer);
	            if(trailer == null) return null;
	            if (maxChunkSize == 0) {
	                // Chunked encoding disabled.
	                return reset(decodeState);
	            } else {
	                reset(decodeState);
	                decodeState.setObject(trailer);
	                // The last chunk, which is empty
	                return decodeState;
	            }
	        }
	    	default: {
	            throw new Error("Shouldn't reach here.");
	        }
    	}
    }
    
    protected abstract HTTPMessage createMessage(String[] initialLine) throws Exception;
    
    private DecodeState reset(DecodeState decodeState) {
    	HTTPMessage message = this.message;
        IoBuffer content = this.content;

        if (content != null) {
            message.setContent(content);
            this.content = null;
        }
        this.message = null;
        decodeState.setState(DecodeState.ENOUGH);
        decodeState.setObject(message);
        state = State.SKIP_CONTROL_CHARS;
        return decodeState;
    }
    
	private DecodeState noreset(DecodeState decodeState) {

		if (content != null) {
            message.setContent(content);
            this.content = null;
        }
		decodeState.setState(DecodeState.ENOUGH);
		decodeState.setObject(message);
		return decodeState;
	}
    
    private boolean readFixedLengthContent(IoBuffer buffer, AtomicBoolean reset) {
        long length = HTTPHeaders.getContentLength(message, -1);
        assert length <= Integer.MAX_VALUE;
        
        byte[] temp;
        if(length > buffer.remaining()) { 
        	// fix Content-Length header value of 32767 and accept application/x-rtsp-tunnelled
        	// and we not reset current parse state
        	if(message.getHeader(CONTENT_TYPE).equalsIgnoreCase("application/x-rtsp-tunnelled")) {
        		length = buffer.remaining(); //Ignore content-length
        		temp = new byte[(int)length];
                buffer.get(temp);
                reset.set(false);
        	} else {
        		return false;
        	}
        } else {
        	temp = new byte[(int)length];
            buffer.get(temp);
        }
        
        if (content == null) {        	
            content = IoBuffer.wrap(temp);
        } else {
            content.put(temp);
        }
        return true;
    }
    
    private int getChunkSize(String hex) {
        hex = hex.trim();
        for (int i = 0; i < hex.length(); i ++) {
            char c = hex.charAt(i);
            if (c == ';' || Character.isWhitespace(c) || Character.isISOControl(c)) {
                hex = hex.substring(0, i);
                break;
            }
        }

        return Integer.parseInt(hex, 16);
    }
    
    private HTTPChunkTrailer readTrailingHeaders(IoBuffer buffer) {
        headerSize = 0;
        int pos = buffer.position();
        String line = readHeader(buffer);
        String lastHeader = null;
        HTTPChunkTrailer trailer = null;
        if (line != null && line.length() != 0) {
        	trailer = new DefaultHttpChunkTrailer();
            do {
                char firstChar = line.charAt(0);
                if (lastHeader != null && (firstChar == ' ' || firstChar == '\t')) {
                    List<String> current = trailer.getHeaders(lastHeader);
                    if (current.size() != 0) {
                        int lastPos = current.size() - 1;
                        String newString = current.get(lastPos) + line.trim();
                        current.set(lastPos, newString);
                    } else {
                        // Content-Length, Transfer-Encoding, or Trailer
                    }
                } else {
                    String[] header = splitHeader(line);
                    String name = header[0];
                    if (!name.equalsIgnoreCase(HTTPHeaders.Names.CONTENT_LENGTH) &&
                        !name.equalsIgnoreCase(HTTPHeaders.Names.TRANSFER_ENCODING) &&
                        !name.equalsIgnoreCase(HTTPHeaders.Names.TRAILER)) {
                        trailer.addHeader(name, header[1]);
                    }
                    lastHeader = name;
                }

                line = readHeader(buffer);
            } while (line != null && line.length() != 0);
        }
        if(line == null) {
        	buffer.position(pos);
        	return null;
        }
        if(trailer != null) 
        	return trailer;
        return HTTPChunk.LAST_CHUNK;
    }
    
    private State readHeaders(IoBuffer buffer) {
        headerSize = 0;
        final HTTPMessage message = this.message;
        String line = readHeader(buffer);
        String name = null;
        String value = null;
        if (line != null && line.length() != 0) {
            message.clearHeaders();
            do {
                char firstChar = line.charAt(0);
                if (name != null && (firstChar == ' ' || firstChar == '\t')) {
                    value = value + ' ' + line.trim();
                } else {
                    if (name != null) {
                        message.addHeader(name, value);
                    }
                    String[] header = splitHeader(line);
                    name = header[0];
                    value = header[1];
                }
                line = readHeader(buffer);
            } while (line != null && line.length() != 0);
            
            
            // Add the last header.
            if (name != null) {
                message.addHeader(name, value);
            } else {
            	return null;
            }
        }
        
        if(line == null) return null;
        
        State nextState;

        if (isContentAlwaysEmpty(message)) {
            nextState = State.SKIP_CONTROL_CHARS;
        } else if (message.isChunked()) {
            // HttpMessage.isChunked() returns true when either:
            // 1) HttpMessage.setChunked(true) was called or
            // 2) 'Transfer-Encoding' is 'chunked'.
            // Because this decoder did not call HttpMessage.setChunked(true)
            // yet, HttpMessage.isChunked() should return true only when
            // 'Transfer-Encoding' is 'chunked'.
            nextState = State.READ_CHUNK_SIZE;
        } else if (HTTPHeaders.getContentLength(message, -1) >= 0) {
            nextState = State.READ_FIXED_LENGTH_CONTENT;
        } else {
            nextState = State.READ_VARIABLE_LENGTH_CONTENT;
        }
        return nextState;
    }
    
    private String readHeader(IoBuffer buffer) {
        StringBuilder sb = this.sb;
        sb.setLength(0);
        int headerSize = this.headerSize;
        int pos = buffer.position();
        
        loop:
        for (;;) {
        	if(buffer.remaining() > 1) {
	            char nextByte = (char) buffer.get();
	            headerSize ++;
	
	            switch (nextByte) {
	            case HTTPCodecUtil.CR:
	                nextByte = (char) buffer.get();
	                headerSize ++;
	                if (nextByte == HTTPCodecUtil.LF) {
	                    break loop;
	                }
	                break;
	            case HTTPCodecUtil.LF:
	                break loop;
	            }
	            sb.append(nextByte);
        	} else {
        		buffer.position(pos);
        		return null;
        	}
        }

        this.headerSize = headerSize;
        return sb.toString();
    }
    
    private String[] splitHeader(String sb) {
        final int length = sb.length();
        int nameStart;
        int nameEnd;
        int colonEnd;
        int valueStart;
        int valueEnd;

        nameStart = findNonWhitespace(sb, 0);
        for (nameEnd = nameStart; nameEnd < length; nameEnd ++) {
            char ch = sb.charAt(nameEnd);
            if (ch == ':' || Character.isWhitespace(ch)) {
                break;
            }
        }

        for (colonEnd = nameEnd; colonEnd < length; colonEnd ++) {
            if (sb.charAt(colonEnd) == ':') {
                colonEnd ++;
                break;
            }
        }

        valueStart = findNonWhitespace(sb, colonEnd);
        if (valueStart == length) {
            return new String[] {
                    sb.substring(nameStart, nameEnd),
                    ""
            };
        }

        valueEnd = findEndOfString(sb);
        return new String[] {
                sb.substring(nameStart, nameEnd),
                sb.substring(valueStart, valueEnd)
        };
    }
    
    protected boolean isContentAlwaysEmpty(HTTPMessage msg) {
        if (msg instanceof HTTPResponse) {
            HTTPResponse res = (HTTPResponse) msg;
            int code = res.getStatus().getCode();
            
            // Correctly handle return codes of 1xx.
            // 
            // See: 
            //     - http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html Section 4.4
            //     - https://github.com/netty/netty/issues/222
            if (code >= 100 && code < 200) {
                if (code == 101 && !res.containsHeader(HTTPHeaders.Names.SEC_WEBSOCKET_ACCEPT)) {
                    // It's Hixie 76 websocket handshake response
                    return false;
                 }
                return true;
            }

            switch (code) {
            case 204: case 205: case 304:
                return true;
            }
        }
        return false;
    }
    
    private void skipControlCharacters(IoBuffer buffer) {
        for (;;) {
            char c = (char) buffer.getUnsigned();
            if (!Character.isISOControl(c) &&
                !Character.isWhitespace(c)) {
                buffer.position(buffer.position() - 1);
                break;
            }
        }
    }
    
    private String readLine(IoBuffer buffer) {
    	StringBuilder sb = this.sb;
        sb.setLength(0);
        int pos = buffer.position();
        while (buffer.remaining() > 1) {
            byte nextByte = buffer.get();
            if (nextByte == HTTPCodecUtil.CR) {
                nextByte = buffer.get();
                if (nextByte == HTTPCodecUtil.LF) {
                    return sb.toString();
                }
            } else if (nextByte == HTTPCodecUtil.LF) {
                return sb.toString();
            } else {                
                sb.append((char) nextByte);
            }
        } 
        
    	buffer.position(pos);
    	return null;
    }
    
    private String[] splitInitialLine(String sb) {
        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;

        aStart = findNonWhitespace(sb, 0);
        aEnd = findWhitespace(sb, aStart);

        bStart = findNonWhitespace(sb, aEnd);
        bEnd = findWhitespace(sb, bStart);

        cStart = findNonWhitespace(sb, bEnd);
        cEnd = findEndOfString(sb);

        return new String[] {
                sb.substring(aStart, aEnd),
                sb.substring(bStart, bEnd),
                cStart < cEnd? sb.substring(cStart, cEnd) : "" };
    }
    
    private int findNonWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (!Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private int findWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private int findEndOfString(String sb) {
        int result;
        for (result = sb.length(); result > 0; result --) {
            if (!Character.isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }
}
