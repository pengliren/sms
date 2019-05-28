package com.sms.server.compression;

import java.io.IOException;

/**
 * An {@link IOException} that is raised when compression or decompression
 * failed.
 */
public class CompressionException extends RuntimeException {

    private static final long serialVersionUID = 5603413481274811897L;

    /**
     * Creates a new instance.
     */
    public CompressionException() {
    }

    /**
     * Creates a new instance.
     */
    public CompressionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public CompressionException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public CompressionException(Throwable cause) {
        super(cause);
    }
}
