package com.sms.server.net.http.message;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Default Http Chunk
 * @author pengliren
 *
 */
public class DefaultHttpChunk implements HTTPChunk {

    private IoBuffer content;
    private boolean last;

    /**
     * Creates a new instance with the specified chunk content. If an empty
     * buffer is specified, this chunk becomes the 'end of content' marker.
     */
    public DefaultHttpChunk(IoBuffer content) {
        setContent(content);
    }

    @Override
    public IoBuffer getContent() {
        return content;
    }

    @Override
    public void setContent(IoBuffer content) {
        if (content == null) {
            throw new NullPointerException("content");
        }
        last = content.remaining() == 0;
        this.content = content;
    }

    @Override
    public boolean isLast() {
        return last;
    }
}
