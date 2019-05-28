package com.sms.server.net.http.codec;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.net.http.message.HTTPHeaders;
import com.sms.server.net.http.message.HTTPMessage;
import com.sms.server.util.CharsetUtil;

/**
 * HTTP Codec Util
 * @author pengliren
 *
 */
public class HTTPCodecUtil {

	//space ' '
    static final byte SP = 32;

    //tab ' '
    static final byte HT = 9;

    /**
     * Carriage return
     */
    static final byte CR = 13;

    /**
     * Equals '='
     */
    static final byte EQUALS = 61;

    /**
     * Line feed character
     */
    static final byte LF = 10;

    /**
     * carriage return line feed
     */
    static final byte[] CRLF = { CR, LF };

    /**
    * Colon ':'
    */
    static final byte COLON = 58;

    /**
    * Semicolon ';'
    */
    static final byte SEMICOLON = 59;

     /**
    * comma ','
    */
    static final byte COMMA = 44;

    static final byte DOUBLE_QUOTE = '"';

    static final Charset DEFAULT_CHARSET = CharsetUtil.UTF_8;

    private HTTPCodecUtil() {
    }

    public static void validateHeaderName(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        for (int i = 0; i < name.length(); i ++) {
            char c = name.charAt(i);
            if (c > 127) {
                throw new IllegalArgumentException(
                        "name contains non-ascii character: " + name);
            }

            // Check prohibited characters.
            switch (c) {
            case '\t': case '\n': case 0x0b: case '\f': case '\r':
            case ' ':  case ',':  case ':':  case ';':  case '=':
                throw new IllegalArgumentException(
                        "name contains one of the following prohibited characters: " +
                        "=,;: \\t\\r\\n\\v\\f: " + name);
            }
        }
    }

    public static void validateHeaderValue(String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        // 0 - the previous character was neither CR nor LF
        // 1 - the previous character was CR
        // 2 - the previous character was LF
        int state = 0;

        for (int i = 0; i < value.length(); i ++) {
            char c = value.charAt(i);

            // Check the absolutely prohibited characters.
            switch (c) {
            case 0x0b: // Vertical tab
                throw new IllegalArgumentException(
                        "value contains a prohibited character '\\v': " + value);
            case '\f':
                throw new IllegalArgumentException(
                        "value contains a prohibited character '\\f': " + value);
            }

            // Check the CRLF (HT | SP) pattern
            switch (state) {
            case 0:
                switch (c) {
                case '\r':
                    state = 1;
                    break;
                case '\n':
                    state = 2;
                    break;
                }
                break;
            case 1:
                switch (c) {
                case '\n':
                    state = 2;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Only '\\n' is allowed after '\\r': " + value);
                }
                break;
            case 2:
                switch (c) {
                case '\t': case ' ':
                    state = 0;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Only ' ' and '\\t' are allowed after '\\n': " + value);
                }
            }
        }

        if (state != 0) {
            throw new IllegalArgumentException(
                    "value must not end with '\\r' or '\\n':" + value);
        }
    }

    public static boolean isTransferEncodingChunked(HTTPMessage m) {
        List<String> chunked = m.getHeaders(HTTPHeaders.Names.TRANSFER_ENCODING);
        if (chunked.isEmpty()) {
            return false;
        }

        for (String v: chunked) {
            if (v.equalsIgnoreCase(HTTPHeaders.Values.CHUNKED)) {
                return true;
            }
        }
        return false;
    }
    
    public static IoBuffer encodeBody(String body) {
		
		IoBuffer buffer = IoBuffer.allocate(512);
		buffer.setAutoExpand(true);
		try {
			buffer.putString(body, DEFAULT_CHARSET.newEncoder());
		} catch (CharacterCodingException e) {
			return null;
		}
		buffer.flip();
		return buffer;
	}
    
    public static String decodeBody(IoBuffer buffer) {
    	
    	if(buffer == null) return null;
    	String body = null;
		try {
			body = buffer.getString(DEFAULT_CHARSET.newDecoder());
		} catch (CharacterCodingException e) {
			return null;
		}
		return body;
    }
}
