package com.sms.server.net.rtsp.message;

import com.sms.server.net.http.message.HTTPHeaders;

/**
 * RTSP Headers
 * @author pengliren
 *
 */
public final class RTSPHeaders {

	/**
     * Standard RTSP header names.
     */
    public static final class Names {
        /**
         * {@code "Accept"}
         */
        public static final String ACCEPT = HTTPHeaders.Names.ACCEPT;
        /**
         * {@code "Accept-Encoding"}
         */
        public static final String ACCEPT_ENCODING = HTTPHeaders.Names.ACCEPT_ENCODING;
        /**
         * {@code "Accept-Lanugage"}
         */
        public static final String ACCEPT_LANGUAGE = HTTPHeaders.Names.ACCEPT_LANGUAGE;
        /**
         * {@code "Allow"}
         */
        public static final String ALLOW = "Allow";
        /**
         * {@code "Authorization"}
         */
        public static final String AUTHORIZATION = HTTPHeaders.Names.AUTHORIZATION;
        /**
         * {@code "Bandwidth"}
         */
        public static final String BANDWIDTH = "Bandwidth";
        /**
         * {@code "Blocksize"}
         */
        public static final String BLOCKSIZE = "Blocksize";
        /**
         * {@code "Cache-Control"}
         */
        public static final String CACHE_CONTROL = HTTPHeaders.Names.CACHE_CONTROL;
        /**
         * {@code "Conference"}
         */
        public static final String CONFERENCE = "Conference";
        /**
         * {@code "Connection"}
         */
        public static final String CONNECTION = HTTPHeaders.Names.CONNECTION;
        /**
         * {@code "Content-Base"}
         */
        public static final String CONTENT_BASE = HTTPHeaders.Names.CONTENT_BASE;
        /**
         * {@code "Content-Encoding"}
         */
        public static final String CONTENT_ENCODING = HTTPHeaders.Names.CONTENT_ENCODING;
        /**
         * {@code "Content-Language"}
         */
        public static final String CONTENT_LANGUAGE = HTTPHeaders.Names.CONTENT_LANGUAGE;
        /**
         * {@code "Content-Length"}
         */
        public static final String CONTENT_LENGTH = HTTPHeaders.Names.CONTENT_LENGTH;
        /**
         * {@code "Content-Location"}
         */
        public static final String CONTENT_LOCATION = HTTPHeaders.Names.CONTENT_LOCATION;
        /**
         * {@code "Content-Type"}
         */
        public static final String CONTENT_TYPE = HTTPHeaders.Names.CONTENT_TYPE;
        /**
         * {@code "CSeq"}
         */
        public static final String CSEQ = "CSeq";
        /**
         * {@code "Date"}
         */
        public static final String DATE = HTTPHeaders.Names.DATE;
        /**
         * {@code "Expires"}
         */
        public static final String EXPIRES = HTTPHeaders.Names.EXPIRES;
        /**
         * {@code "From"}
         */
        public static final String FROM = HTTPHeaders.Names.FROM;
        /**
         * {@code "Host"}
         */
        public static final String HOST = HTTPHeaders.Names.HOST;
        /**
         * {@code "If-Match"}
         */
        public static final String IF_MATCH = HTTPHeaders.Names.IF_MATCH;
        /**
         * {@code "If-Modified-Since"}
         */
        public static final String IF_MODIFIED_SINCE = HTTPHeaders.Names.IF_MODIFIED_SINCE;
        /**
         * {@code "KeyMgmt"}
         */
        public static final String KEYMGMT = "KeyMgmt";
        /**
         * {@code "Last-Modified"}
         */
        public static final String LAST_MODIFIED = HTTPHeaders.Names.LAST_MODIFIED;
        /**
         * {@code "Proxy-Authenticate"}
         */
        public static final String PROXY_AUTHENTICATE = HTTPHeaders.Names.PROXY_AUTHENTICATE;
        /**
         * {@code "Proxy-Require"}
         */
        public static final String PROXY_REQUIRE = "Proxy-Require";
        /**
         * {@code "Public"}
         */
        public static final String PUBLIC = "Public";
        /**
         * {@code "Range"}
         */
        public static final String RANGE = HTTPHeaders.Names.RANGE;
        /**
         * {@code "Referer"}
         */
        public static final String REFERER = HTTPHeaders.Names.REFERER;
        /**
         * {@code "Require"}
         */
        public static final String REQUIRE = "Require";
        /**
         * {@code "Retry-After"}
         */
        public static final String RETRT_AFTER = HTTPHeaders.Names.RETRY_AFTER;
        /**
         * {@code "RTP-Info"}
         */
        public static final String RTP_INFO = "RTP-Info";
        /**
         * {@code "Scale"}
         */
        public static final String SCALE = "Scale";
        /**
         * {@code "Session"}
         */
        public static final String SESSION = "Session";
        /**
         * {@code "Server"}
         */
        public static final String SERVER = HTTPHeaders.Names.SERVER;
        /**
         * {@code "Speed"}
         */
        public static final String SPEED = "Speed";
        /**
         * {@code "Timestamp"}
         */
        public static final String TIMESTAMP = "Timestamp";
        /**
         * {@code "Transport"}
         */
        public static final String TRANSPORT = "Transport";
        /**
         * {@code "Unsupported"}
         */
        public static final String UNSUPPORTED = "Unsupported";
        /**
         * {@code "User-Agent"}
         */
        public static final String USER_AGENT = HTTPHeaders.Names.USER_AGENT;
        /**
         * {@code "Vary"}
         */
        public static final String VARY = HTTPHeaders.Names.VARY;
        /**
         * {@code "Via"}
         */
        public static final String VIA = HTTPHeaders.Names.VIA;
        /**
         * {@code "WWW-Authenticate"}
         */
        public static final String WWW_AUTHENTICATE = HTTPHeaders.Names.WWW_AUTHENTICATE;

        private Names() {
        }
    }

    /**
     * Standard RTSP header values.
     */
    public static final class Values {
        /**
         * {@code "append"}
         */
        public static final String APPEND = "append";
        /**
         * {@code "AVP"}
         */
        public static final String AVP = "AVP";
        /**
         * {@code "bytes"}
         */
        public static final String BYTES = HTTPHeaders.Values.BYTES;
        /**
         * {@code "charset"}
         */
        public static final String CHARSET = HTTPHeaders.Values.CHARSET;
        /**
         * {@code "client_port"}
         */
        public static final String CLIENT_PORT = "client_port";
        /**
         * {@code "clock"}
         */
        public static final String CLOCK = "clock";
        /**
         * {@code "close"}
         */
        public static final String CLOSE = HTTPHeaders.Values.CLOSE;
        /**
         * {@code "compress"}
         */
        public static final String COMPRESS = HTTPHeaders.Values.COMPRESS;
        /**
         * {@code "100-continue"}
         */
        public static final String CONTINUE =  HTTPHeaders.Values.CONTINUE;
        /**
         * {@code "deflate"}
         */
        public static final String DEFLATE = HTTPHeaders.Values.DEFLATE;
        /**
         * {@code "destination"}
         */
        public static final String DESTINATION = "destination";
        /**
         * {@code "gzip"}
         */
        public static final String GZIP = HTTPHeaders.Values.GZIP;
        /**
         * {@code "identity"}
         */
        public static final String IDENTITY = HTTPHeaders.Values.IDENTITY;
        /**
         * {@code "interleaved"}
         */
        public static final String INTERLEAVED = "interleaved";
        /**
         * {@code "keep-alive"}
         */
        public static final String KEEP_ALIVE = HTTPHeaders.Values.KEEP_ALIVE;
        /**
         * {@code "layers"}
         */
        public static final String LAYERS = "layers";
        /**
         * {@code "max-age"}
         */
        public static final String MAX_AGE = HTTPHeaders.Values.MAX_AGE;
        /**
         * {@code "max-stale"}
         */
        public static final String MAX_STALE = HTTPHeaders.Values.MAX_STALE;
        /**
         * {@code "min-fresh"}
         */
        public static final String MIN_FRESH = HTTPHeaders.Values.MIN_FRESH;
        /**
         * {@code "mode"}
         */
        public static final String MODE = "mode";
        /**
         * {@code "multicast"}
         */
        public static final String MULTICAST = "multicast";
        /**
         * {@code "must-revalidate"}
         */
        public static final String MUST_REVALIDATE = HTTPHeaders.Values.MUST_REVALIDATE;
        /**
         * {@code "none"}
         */
        public static final String NONE = HTTPHeaders.Values.NONE;
        /**
         * {@code "no-cache"}
         */
        public static final String NO_CACHE = HTTPHeaders.Values.NO_CACHE;
        /**
         * {@code "no-transform"}
         */
        public static final String NO_TRANSFORM = HTTPHeaders.Values.NO_TRANSFORM;
        /**
         * {@code "only-if-cached"}
         */
        public static final String ONLY_IF_CACHED = HTTPHeaders.Values.ONLY_IF_CACHED;
        /**
         * {@code "port"}
         */
        public static final String PORT = "port";
        /**
         * {@code "private"}
         */
        public static final String PRIVATE = HTTPHeaders.Values.PRIVATE;
        /**
         * {@code "proxy-revalidate"}
         */
        public static final String PROXY_REVALIDATE = HTTPHeaders.Values.PROXY_REVALIDATE;
        /**
         * {@code "public"}
         */
        public static final String PUBLIC = HTTPHeaders.Values.PUBLIC;
        /**
         * {@code "RTP"}
         */
        public static final String RTP = "RTP";
        /**
         * {@code "rtptime"}
         */
        public static final String RTPTIME = "rtptime";
        /**
         * {@code "seq"}
         */
        public static final String SEQ = "seq";
        /**
         * {@code "server_port"}
         */
        public static final String SERVER_PORT = "server_port";
        /**
         * {@code "ssrc"}
         */
        public static final String SSRC = "ssrc";
        /**
         * {@code "TCP"}
         */
        public static final String TCP = "TCP";
        /**
         * {@code "time"}
         */
        public static final String TIME = "time";
        /**
         * {@code "timeout"}
         */
        public static final String TIMEOUT = "timeout";
        /**
         * {@code "ttl"}
         */
        public static final String TTL = "ttl";
        /**
         * {@code "UDP"}
         */
        public static final String UDP = "UDP";
        /**
         * {@code "unicast"}
         */
        public static final String UNICAST = "unicast";
        /**
         * {@code "url"}
         */
        public static final String URL = "url";

        protected Values() {
        }
    }

    private RTSPHeaders() {
    }
}
