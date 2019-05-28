package com.sms.server.net.http.message;

/**
 * HTTP Response Status
 * @author pengliren
 *
 */
public class HTTPResponseStatus implements Comparable<HTTPResponseStatus> {

    /**
     * 100 Continue
     */
    public static final HTTPResponseStatus CONTINUE = new HTTPResponseStatus(100, "Continue");

    /**
     * 101 Switching Protocols
     */
    public static final HTTPResponseStatus SWITCHING_PROTOCOLS = new HTTPResponseStatus(101, "Switching Protocols");

    /**
     * 102 Processing (WebDAV, RFC2518)
     */
    public static final HTTPResponseStatus PROCESSING = new HTTPResponseStatus(102, "Processing");

    /**
     * 200 OK
     */
    public static final HTTPResponseStatus OK = new HTTPResponseStatus(200, "OK");

    /**
     * 201 Created
     */
    public static final HTTPResponseStatus CREATED = new HTTPResponseStatus(201, "Created");

    /**
     * 202 Accepted
     */
    public static final HTTPResponseStatus ACCEPTED = new HTTPResponseStatus(202, "Accepted");

    /**
     * 203 Non-Authoritative Information (since HTTP/1.1)
     */
    public static final HTTPResponseStatus NON_AUTHORITATIVE_INFORMATION = new HTTPResponseStatus(203, "Non-Authoritative Information");

    /**
     * 204 No Content
     */
    public static final HTTPResponseStatus NO_CONTENT = new HTTPResponseStatus(204, "No Content");

    /**
     * 205 Reset Content
     */
    public static final HTTPResponseStatus RESET_CONTENT = new HTTPResponseStatus(205, "Reset Content");

    /**
     * 206 Partial Content
     */
    public static final HTTPResponseStatus PARTIAL_CONTENT = new HTTPResponseStatus(206, "Partial Content");

    /**
     * 207 Multi-Status (WebDAV, RFC2518)
     */
    public static final HTTPResponseStatus MULTI_STATUS = new HTTPResponseStatus(207, "Multi-Status");

    /**
     * 300 Multiple Choices
     */
    public static final HTTPResponseStatus MULTIPLE_CHOICES = new HTTPResponseStatus(300, "Multiple Choices");

    /**
     * 301 Moved Permanently
     */
    public static final HTTPResponseStatus MOVED_PERMANENTLY = new HTTPResponseStatus(301, "Moved Permanently");

    /**
     * 302 Found
     */
    public static final HTTPResponseStatus FOUND = new HTTPResponseStatus(302, "Found");

    /**
     * 303 See Other (since HTTP/1.1)
     */
    public static final HTTPResponseStatus SEE_OTHER = new HTTPResponseStatus(303, "See Other");

    /**
     * 304 Not Modified
     */
    public static final HTTPResponseStatus NOT_MODIFIED = new HTTPResponseStatus(304, "Not Modified");

    /**
     * 305 Use Proxy (since HTTP/1.1)
     */
    public static final HTTPResponseStatus USE_PROXY = new HTTPResponseStatus(305, "Use Proxy");

    /**
     * 307 Temporary Redirect (since HTTP/1.1)
     */
    public static final HTTPResponseStatus TEMPORARY_REDIRECT = new HTTPResponseStatus(307, "Temporary Redirect");

    /**
     * 400 Bad Request
     */
    public static final HTTPResponseStatus BAD_REQUEST = new HTTPResponseStatus(400, "Bad Request");

    /**
     * 401 Unauthorized
     */
    public static final HTTPResponseStatus UNAUTHORIZED = new HTTPResponseStatus(401, "Unauthorized");

    /**
     * 402 Payment Required
     */
    public static final HTTPResponseStatus PAYMENT_REQUIRED = new HTTPResponseStatus(402, "Payment Required");

    /**
     * 403 Forbidden
     */
    public static final HTTPResponseStatus FORBIDDEN = new HTTPResponseStatus(403, "Forbidden");

    /**
     * 404 Not Found
     */
    public static final HTTPResponseStatus NOT_FOUND = new HTTPResponseStatus(404, "Not Found");

    /**
     * 405 Method Not Allowed
     */
    public static final HTTPResponseStatus METHOD_NOT_ALLOWED = new HTTPResponseStatus(405, "Method Not Allowed");

    /**
     * 406 Not Acceptable
     */
    public static final HTTPResponseStatus NOT_ACCEPTABLE = new HTTPResponseStatus(406, "Not Acceptable");

    /**
     * 407 Proxy Authentication Required
     */
    public static final HTTPResponseStatus PROXY_AUTHENTICATION_REQUIRED = new HTTPResponseStatus(407, "Proxy Authentication Required");

    /**
     * 408 Request Timeout
     */
    public static final HTTPResponseStatus REQUEST_TIMEOUT = new HTTPResponseStatus(408, "Request Timeout");

    /**
     * 409 Conflict
     */
    public static final HTTPResponseStatus CONFLICT = new HTTPResponseStatus(409, "Conflict");

    /**
     * 410 Gone
     */
    public static final HTTPResponseStatus GONE = new HTTPResponseStatus(410, "Gone");

    /**
     * 411 Length Required
     */
    public static final HTTPResponseStatus LENGTH_REQUIRED = new HTTPResponseStatus(411, "Length Required");

    /**
     * 412 Precondition Failed
     */
    public static final HTTPResponseStatus PRECONDITION_FAILED = new HTTPResponseStatus(412, "Precondition Failed");

    /**
     * 413 Request Entity Too Large
     */
    public static final HTTPResponseStatus REQUEST_ENTITY_TOO_LARGE = new HTTPResponseStatus(413, "Request Entity Too Large");

    /**
     * 414 Request-URI Too Long
     */
    public static final HTTPResponseStatus REQUEST_URI_TOO_LONG = new HTTPResponseStatus(414, "Request-URI Too Long");

    /**
     * 415 Unsupported Media Type
     */
    public static final HTTPResponseStatus UNSUPPORTED_MEDIA_TYPE = new HTTPResponseStatus(415, "Unsupported Media Type");

    /**
     * 416 Requested Range Not Satisfiable
     */
    public static final HTTPResponseStatus REQUESTED_RANGE_NOT_SATISFIABLE = new HTTPResponseStatus(416, "Requested Range Not Satisfiable");

    /**
     * 417 Expectation Failed
     */
    public static final HTTPResponseStatus EXPECTATION_FAILED = new HTTPResponseStatus(417, "Expectation Failed");

    /**
     * 422 Unprocessable Entity (WebDAV, RFC4918)
     */
    public static final HTTPResponseStatus UNPROCESSABLE_ENTITY = new HTTPResponseStatus(422, "Unprocessable Entity");

    /**
     * 423 Locked (WebDAV, RFC4918)
     */
    public static final HTTPResponseStatus LOCKED = new HTTPResponseStatus(423, "Locked");

    /**
     * 424 Failed Dependency (WebDAV, RFC4918)
     */
    public static final HTTPResponseStatus FAILED_DEPENDENCY = new HTTPResponseStatus(424, "Failed Dependency");

    /**
     * 425 Unordered Collection (WebDAV, RFC3648)
     */
    public static final HTTPResponseStatus UNORDERED_COLLECTION = new HTTPResponseStatus(425, "Unordered Collection");

    /**
     * 426 Upgrade Required (RFC2817)
     */
    public static final HTTPResponseStatus UPGRADE_REQUIRED = new HTTPResponseStatus(426, "Upgrade Required");

    /**
     * 500 Internal Server Error
     */
    public static final HTTPResponseStatus INTERNAL_SERVER_ERROR = new HTTPResponseStatus(500, "Internal Server Error");

    /**
     * 501 Not Implemented
     */
    public static final HTTPResponseStatus NOT_IMPLEMENTED = new HTTPResponseStatus(501, "Not Implemented");

    /**
     * 502 Bad Gateway
     */
    public static final HTTPResponseStatus BAD_GATEWAY = new HTTPResponseStatus(502, "Bad Gateway");

    /**
     * 503 Service Unavailable
     */
    public static final HTTPResponseStatus SERVICE_UNAVAILABLE = new HTTPResponseStatus(503, "Service Unavailable");

    /**
     * 504 Gateway Timeout
     */
    public static final HTTPResponseStatus GATEWAY_TIMEOUT = new HTTPResponseStatus(504, "Gateway Timeout");

    /**
     * 505 HTTP Version Not Supported
     */
    public static final HTTPResponseStatus HTTP_VERSION_NOT_SUPPORTED = new HTTPResponseStatus(505, "HTTP Version Not Supported");

    /**
     * 506 Variant Also Negotiates (RFC2295)
     */
    public static final HTTPResponseStatus VARIANT_ALSO_NEGOTIATES = new HTTPResponseStatus(506, "Variant Also Negotiates");

    /**
     * 507 Insufficient Storage (WebDAV, RFC4918)
     */
    public static final HTTPResponseStatus INSUFFICIENT_STORAGE = new HTTPResponseStatus(507, "Insufficient Storage");

    /**
     * 510 Not Extended (RFC2774)
     */
    public static final HTTPResponseStatus NOT_EXTENDED = new HTTPResponseStatus(510, "Not Extended");

    /**
     * Returns the {@link HTTPResponseStatus} represented by the specified code.
     * If the specified code is a standard HTTP status code, a cached instance
     * will be returned.  Otherwise, a new instance will be returned.
     */
    public static HTTPResponseStatus valueOf(int code) {
        switch (code) {
        case 100:
            return CONTINUE;
        case 101:
            return SWITCHING_PROTOCOLS;
        case 102:
            return PROCESSING;
        case 200:
            return OK;
        case 201:
            return CREATED;
        case 202:
            return ACCEPTED;
        case 203:
            return NON_AUTHORITATIVE_INFORMATION;
        case 204:
            return NO_CONTENT;
        case 205:
            return RESET_CONTENT;
        case 206:
            return PARTIAL_CONTENT;
        case 207:
            return MULTI_STATUS;
        case 300:
            return MULTIPLE_CHOICES;
        case 301:
            return MOVED_PERMANENTLY;
        case 302:
            return FOUND;
        case 303:
            return SEE_OTHER;
        case 304:
            return NOT_MODIFIED;
        case 305:
            return USE_PROXY;
        case 307:
            return TEMPORARY_REDIRECT;
        case 400:
            return BAD_REQUEST;
        case 401:
            return UNAUTHORIZED;
        case 402:
            return PAYMENT_REQUIRED;
        case 403:
            return FORBIDDEN;
        case 404:
            return NOT_FOUND;
        case 405:
            return METHOD_NOT_ALLOWED;
        case 406:
            return NOT_ACCEPTABLE;
        case 407:
            return PROXY_AUTHENTICATION_REQUIRED;
        case 408:
            return REQUEST_TIMEOUT;
        case 409:
            return CONFLICT;
        case 410:
            return GONE;
        case 411:
            return LENGTH_REQUIRED;
        case 412:
            return PRECONDITION_FAILED;
        case 413:
            return REQUEST_ENTITY_TOO_LARGE;
        case 414:
            return REQUEST_URI_TOO_LONG;
        case 415:
            return UNSUPPORTED_MEDIA_TYPE;
        case 416:
            return REQUESTED_RANGE_NOT_SATISFIABLE;
        case 417:
            return EXPECTATION_FAILED;
        case 422:
            return UNPROCESSABLE_ENTITY;
        case 423:
            return LOCKED;
        case 424:
            return FAILED_DEPENDENCY;
        case 425:
            return UNORDERED_COLLECTION;
        case 426:
            return UPGRADE_REQUIRED;
        case 500:
            return INTERNAL_SERVER_ERROR;
        case 501:
            return NOT_IMPLEMENTED;
        case 502:
            return BAD_GATEWAY;
        case 503:
            return SERVICE_UNAVAILABLE;
        case 504:
            return GATEWAY_TIMEOUT;
        case 505:
            return HTTP_VERSION_NOT_SUPPORTED;
        case 506:
            return VARIANT_ALSO_NEGOTIATES;
        case 507:
            return INSUFFICIENT_STORAGE;
        case 510:
            return NOT_EXTENDED;
        }

        final String reasonPhrase;

        if (code < 100) {
            reasonPhrase = "Unknown Status";
        } else if (code < 200) {
            reasonPhrase = "Informational";
        } else if (code < 300) {
            reasonPhrase = "Successful";
        } else if (code < 400) {
            reasonPhrase = "Redirection";
        } else if (code < 500) {
            reasonPhrase = "Client Error";
        } else if (code < 600) {
            reasonPhrase = "Server Error";
        } else {
            reasonPhrase = "Unknown Status";
        }

        return new HTTPResponseStatus(code, reasonPhrase + " (" + code + ')');
    }

    private final int code;

    private final String reasonPhrase;

    /**
     * Creates a new instance with the specified {@code code} and its
     * {@code reasonPhrase}.
     */
    public HTTPResponseStatus(int code, String reasonPhrase) {
        if (code < 0) {
            throw new IllegalArgumentException(
                    "code: " + code + " (expected: 0+)");
        }

        if (reasonPhrase == null) {
            throw new NullPointerException("reasonPhrase");
        }

        for (int i = 0; i < reasonPhrase.length(); i ++) {
            char c = reasonPhrase.charAt(i);
            // Check prohibited characters.
            switch (c) {
            case '\n': case '\r':
                throw new IllegalArgumentException(
                        "reasonPhrase contains one of the following prohibited characters: " +
                        "\\r\\n: " + reasonPhrase);
            }
        }

        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Returns the code of this status.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the reason phrase of this status.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public int hashCode() {
        return getCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HTTPResponseStatus)) {
            return false;
        }

        return getCode() == ((HTTPResponseStatus) o).getCode();
    }

    @Override
    public int compareTo(HTTPResponseStatus o) {
        return getCode() - o.getCode();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(reasonPhrase.length() + 5);
        buf.append(code);
        buf.append(' ');
        buf.append(reasonPhrase);
        return buf.toString();
    }
}
