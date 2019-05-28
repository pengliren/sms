package com.sms.server.net.http.codec;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Query String Encoder
 * @author pengliren
 *
 */
public class QueryStringEncoder {

	private final Charset charset;
    private final String uri;
    private final List<Param> params = new ArrayList<Param>();

    /**
     * Creates a new encoder that encodes a URI that starts with the specified
     * path string.  The encoder will encode the URI in UTF-8.
     */
    public QueryStringEncoder(String uri) {
        this(uri, HTTPCodecUtil.DEFAULT_CHARSET);
    }

    /**
     * Creates a new encoder that encodes a URI that starts with the specified
     * path string in the specified charset.
     */
    public QueryStringEncoder(String uri, Charset charset) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        if (charset == null) {
            throw new NullPointerException("charset");
        }

        this.uri = uri;
        this.charset = charset;
    }

    /**
     * Adds a parameter with the specified name and value to this encoder.
     */
    public void addParam(String name, String value) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
        params.add(new Param(name, value));
    }

    /**
     * Returns the URL-encoded URI object which was created from the path string
     * specified in the constructor and the parameters added by
     * {@link #addParam(String, String)} method.
     */
    public URI toUri() throws URISyntaxException {
        return new URI(toString());
    }

    /**
     * Returns the URL-encoded URI which was created from the path string
     * specified in the constructor and the parameters added by
     * {@link #addParam(String, String)} method.
     */
    @Override
    public String toString() {
        if (params.isEmpty()) {
            return uri;
        } else {
            StringBuilder sb = new StringBuilder(uri).append("?");
            for (int i = 0; i < params.size(); i++) {
                Param param = params.get(i);
                sb.append(encodeComponent(param.name, charset));
                sb.append("=");
                sb.append(encodeComponent(param.value, charset));
                if (i != params.size() - 1) {
                    sb.append("&");
                }
            }
            return sb.toString();
        }
    }

    private static String encodeComponent(String s, Charset charset) {
        try {
            return URLEncoder.encode(s, charset.name()).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(charset.name());
        }
    }

    private static final class Param {

        final String name;
        final String value;

        Param(String name, String value) {
            this.value = value;
            this.name = name;
        }
    }
}
