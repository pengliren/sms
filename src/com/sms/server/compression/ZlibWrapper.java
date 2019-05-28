package com.sms.server.compression;

/**
 * The container file formats that wrap the stream compressed by the DEFLATE
 * algorithm.
 */
public enum ZlibWrapper {
    /**
     * The ZLIB wrapper as specified in <a href="http://tools.ietf.org/html/rfc1950">RFC 1950</a>.
     */
    ZLIB,
    /**
     * The GZIP wrapper as specified in <a href="http://tools.ietf.org/html/rfc1950">RFC 1952</a>.
     */
    GZIP,
    /**
     * Raw DEFLATE stream only (no header and no footer).
     */
    NONE,
    /**
     * Try {@link #ZLIB} first and then {@link #NONE} if the first attempt fails.
     * Please note that you can specify this wrapper type only when decompressing.
     */
    ZLIB_OR_NONE
}
