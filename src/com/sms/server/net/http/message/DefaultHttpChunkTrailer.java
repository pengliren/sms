package com.sms.server.net.http.message;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Default Http Chunk Trailer
 * @author pengliren
 *
 */
public class DefaultHttpChunkTrailer implements HTTPChunkTrailer {

    private final HTTPHeaders headers = new HTTPHeaders() {
        @Override
        void validateHeaderName(String name) {
            super.validateHeaderName(name);
            if (name.equalsIgnoreCase(HTTPHeaders.Names.CONTENT_LENGTH) ||
                name.equalsIgnoreCase(HTTPHeaders.Names.TRANSFER_ENCODING) ||
                name.equalsIgnoreCase(HTTPHeaders.Names.TRAILER)) {
                throw new IllegalArgumentException(
                        "prohibited trailing header: " + name);
            }
        }
    };

    @Override
    public boolean isLast() {
        return true;
    }

    @Override
    public void addHeader(final String name, final Object value) {
        headers.addHeader(name, value);
    }

    @Override
    public void setHeader(final String name, final Object value) {
        headers.setHeader(name, value);
    }

    @Override
    public void setHeader(final String name, final Iterable<?> values) {
        headers.setHeader(name, values);
    }

    @Override
    public void removeHeader(final String name) {
        headers.removeHeader(name);
    }

    @Override
    public void clearHeaders() {
        headers.clearHeaders();
    }

    @Override
    public String getHeader(final String name) {
        return headers.getHeader(name);
    }

    @Override
    public List<String> getHeaders(final String name) {
        return headers.getHeaders(name);
    }

    @Override
    public List<Map.Entry<String, String>> getHeaders() {
        return headers.getHeaders();
    }

    @Override
    public boolean containsHeader(final String name) {
        return headers.containsHeader(name);
    }

    @Override
    public Set<String> getHeaderNames() {
        return headers.getHeaderNames();
    }

    @Override
    public IoBuffer getContent() {
        return null;
    }

    @Override
    public void setContent(IoBuffer content) {
        throw new IllegalStateException("read-only");
    }
}
