package com.sms.server.net.http.message;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * HTTP Chunk Interface
 * @author pengliren
 *
 */
public interface HTTPChunk {

	/**
     * The 'end of content' marker in chunked encoding.
     */
    HTTPChunkTrailer LAST_CHUNK = new HTTPChunkTrailer() {
        @Override
        public IoBuffer getContent() {
            return null;
        }

        @Override
        public void setContent(IoBuffer content) {
            throw new IllegalStateException("read-only");
        }

        @Override
        public boolean isLast() {
            return true;
        }

        @Override
        public void addHeader(String name, Object value) {
            throw new IllegalStateException("read-only");
        }

        @Override
        public void clearHeaders() {
            // NOOP
        }

        @Override
        public boolean containsHeader(String name) {
            return false;
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public Set<String> getHeaderNames() {
            return Collections.emptySet();
        }

        @Override
        public List<String> getHeaders(String name) {
            return Collections.emptyList();
        }

        @Override
        public List<Map.Entry<String, String>> getHeaders() {
            return Collections.emptyList();
        }

        @Override
        public void removeHeader(String name) {
            // NOOP
        }

        @Override
        public void setHeader(String name, Object value) {
            throw new IllegalStateException("read-only");
        }

        @Override
        public void setHeader(String name, Iterable<?> values) {
            throw new IllegalStateException("read-only");
        }
    };

    /**
     * Returns {@code true} if and only if this chunk is the 'end of content'
     * marker.
     */
    boolean isLast();

    /**
     * Returns the content of this chunk.  If this is the 'end of content'
     * marker, {@link ChannelBuffers#EMPTY_BUFFER} will be returned.
     */
    IoBuffer getContent();

    /**
     * Sets the content of this chunk.  If an empty buffer is specified,
     * this chunk becomes the 'end of content' marker.
     */
    void setContent(IoBuffer content);
}
