package com.sms.server.net.rtsp.message;

import com.sms.server.net.http.message.HTTPResponseStatus;

/**
 * RTSP Response Statuses
 * @author pengliren
 *
 */
public final class RTSPResponseStatuses {

	/**
     * 100 Continue
     */
    public static final HTTPResponseStatus CONTINUE = HTTPResponseStatus.CONTINUE;

    /**
     * 200 OK
     */
    public static final HTTPResponseStatus OK = HTTPResponseStatus.OK;

    /**
     * 201 Created
     */
    public static final HTTPResponseStatus CREATED = HTTPResponseStatus.CREATED;

    /**
     * 250 Low on Storage Space
     */
    public static final HTTPResponseStatus LOW_STORAGE_SPACE = new HTTPResponseStatus(
            250, "Low on Storage Space");

    /**
     * 300 Multiple Choices
     */
    public static final HTTPResponseStatus MULTIPLE_CHOICES = HTTPResponseStatus.MULTIPLE_CHOICES;

    /**
     * 301 Moved Permanently
     */
    public static final HTTPResponseStatus MOVED_PERMANENTLY = HTTPResponseStatus.MOVED_PERMANENTLY;

    /**
     * 302 Moved Temporarily
     */
    public static final HTTPResponseStatus MOVED_TEMPORARILY = new HTTPResponseStatus(
            302, "Moved Temporarily");
    /**
     * 304 Not Modified
     */
    public static final HTTPResponseStatus NOT_MODIFIED = HTTPResponseStatus.NOT_MODIFIED;

    /**
     * 305 Use Proxy
     */
    public static final HTTPResponseStatus USE_PROXY = HTTPResponseStatus.USE_PROXY;

    /**
     * 400 Bad Request
     */
    public static final HTTPResponseStatus BAD_REQUEST = HTTPResponseStatus.BAD_REQUEST;

    /**
     * 401 Unauthorized
     */
    public static final HTTPResponseStatus UNAUTHORIZED = HTTPResponseStatus.UNAUTHORIZED;

    /**
     * 402 Payment Required
     */
    public static final HTTPResponseStatus PAYMENT_REQUIRED = HTTPResponseStatus.PAYMENT_REQUIRED;

    /**
     * 403 Forbidden
     */
    public static final HTTPResponseStatus FORBIDDEN = HTTPResponseStatus.FORBIDDEN;

    /**
     * 404 Not Found
     */
    public static final HTTPResponseStatus NOT_FOUND = HTTPResponseStatus.NOT_FOUND;

    /**
     * 405 Method Not Allowed
     */
    public static final HTTPResponseStatus METHOD_NOT_ALLOWED = HTTPResponseStatus.METHOD_NOT_ALLOWED;

    /**
     * 406 Not Acceptable
     */
    public static final HTTPResponseStatus NOT_ACCEPTABLE = HTTPResponseStatus.NOT_ACCEPTABLE;

    /**
     * 407 Proxy Authentication Required
     */
    public static final HTTPResponseStatus PROXY_AUTHENTICATION_REQUIRED = HTTPResponseStatus.PROXY_AUTHENTICATION_REQUIRED;

    /**
     * 408 Request Timeout
     */
    public static final HTTPResponseStatus REQUEST_TIMEOUT = HTTPResponseStatus.REQUEST_TIMEOUT;

    /**
     * 410 Gone
     */
    public static final HTTPResponseStatus GONE = HTTPResponseStatus.GONE;

    /**
     * 411 Length Required
     */
    public static final HTTPResponseStatus LENGTH_REQUIRED = HTTPResponseStatus.LENGTH_REQUIRED;

    /**
     * 412 Precondition Failed
     */
    public static final HTTPResponseStatus PRECONDITION_FAILED = HTTPResponseStatus.PRECONDITION_FAILED;

    /**
     * 413 Request Entity Too Large
     */
    public static final HTTPResponseStatus REQUEST_ENTITY_TOO_LARGE = HTTPResponseStatus.REQUEST_ENTITY_TOO_LARGE;

    /**
     * 414 Request-URI Too Long
     */
    public static final HTTPResponseStatus REQUEST_URI_TOO_LONG = HTTPResponseStatus.REQUEST_URI_TOO_LONG;

    /**
     * 415 Unsupported Media Type
     */
    public static final HTTPResponseStatus UNSUPPORTED_MEDIA_TYPE = HTTPResponseStatus.UNSUPPORTED_MEDIA_TYPE;

    /**
     * 451 Parameter Not Understood
     */
    public static final HTTPResponseStatus PARAMETER_NOT_UNDERSTOOD = new HTTPResponseStatus(
            451, "Parameter Not Understood");

    /**
     * 452 Conference Not Found
     */
    public static final HTTPResponseStatus CONFERENCE_NOT_FOUND = new HTTPResponseStatus(
            452, "Conference Not Found");

    /**
     * 453 Not Enough Bandwidth
     */
    public static final HTTPResponseStatus NOT_ENOUGH_BANDWIDTH = new HTTPResponseStatus(
            453, "Not Enough Bandwidth");

    /**
     * 454 Session Not Found
     */
    public static final HTTPResponseStatus SESSION_NOT_FOUND = new HTTPResponseStatus(
            454, "Session Not Found");

    /**
     * 455 Method Not Valid in This State
     */
    public static final HTTPResponseStatus METHOD_NOT_VALID = new HTTPResponseStatus(
            455, "Method Not Valid in This State");

    /**
     * 456 Header Field Not Valid for Resource
     */
    public static final HTTPResponseStatus HEADER_FIELD_NOT_VALID = new HTTPResponseStatus(
            456, "Header Field Not Valid for Resource");

    /**
     * 457 Invalid Range
     */
    public static final HTTPResponseStatus INVALID_RANGE = new HTTPResponseStatus(
            457, "Invalid Range");

    /**
     * 458 Parameter Is Read-Only
     */
    public static final HTTPResponseStatus PARAMETER_IS_READONLY = new HTTPResponseStatus(
            458, "Parameter Is Read-Only");

    /**
     * 459 Aggregate operation not allowed
     */
    public static final HTTPResponseStatus AGGREGATE_OPERATION_NOT_ALLOWED = new HTTPResponseStatus(
            459, "Aggregate operation not allowed");

    /**
     * 460 Only Aggregate operation allowed
     */
    public static final HTTPResponseStatus ONLY_AGGREGATE_OPERATION_ALLOWED = new HTTPResponseStatus(
            460, "Only Aggregate operation allowed");

    /**
     * 461 Unsupported transport
     */
    public static final HTTPResponseStatus UNSUPPORTED_TRANSPORT = new HTTPResponseStatus(
            461, "Unsupported transport");

    /**
     * 462 Destination unreachable
     */
    public static final HTTPResponseStatus DESTINATION_UNREACHABLE = new HTTPResponseStatus(
            462, "Destination unreachable");

    /**
     * 463 Key management failure
     */
    public static final HTTPResponseStatus KEY_MANAGEMENT_FAILURE = new HTTPResponseStatus(
            463, "Key management failure");

    /**
     * 500 Internal Server Error
     */
    public static final HTTPResponseStatus INTERNAL_SERVER_ERROR = HTTPResponseStatus.INTERNAL_SERVER_ERROR;

    /**
     * 501 Not Implemented
     */
    public static final HTTPResponseStatus NOT_IMPLEMENTED = HTTPResponseStatus.NOT_IMPLEMENTED;

    /**
     * 502 Bad Gateway
     */
    public static final HTTPResponseStatus BAD_GATEWAY = HTTPResponseStatus.BAD_GATEWAY;

    /**
     * 503 Service Unavailable
     */
    public static final HTTPResponseStatus SERVICE_UNAVAILABLE = HTTPResponseStatus.SERVICE_UNAVAILABLE;

    /**
     * 504 Gateway Timeout
     */
    public static final HTTPResponseStatus GATEWAY_TIMEOUT = HTTPResponseStatus.GATEWAY_TIMEOUT;

    /**
     * 505 RTSP Version not supported
     */
    public static final HTTPResponseStatus RTSP_VERSION_NOT_SUPPORTED = new HTTPResponseStatus(
            505, "RTSP Version not supported");

    /**
     * 551 Option not supported
     */
    public static final HTTPResponseStatus OPTION_NOT_SUPPORTED = new HTTPResponseStatus(
            551, "Option not supported");


    /**
     * Returns the {@link HTTPResponseStatus} represented by the specified code.
     * If the specified code is a standard RTSP status code, a cached instance
     * will be returned.  Otherwise, a new instance will be returned.
     */
    public static HTTPResponseStatus valueOf(int code) {
        switch (code) {
        case 250: return LOW_STORAGE_SPACE;
        case 302: return MOVED_TEMPORARILY;
        case 451: return PARAMETER_NOT_UNDERSTOOD;
        case 452: return CONFERENCE_NOT_FOUND;
        case 453: return NOT_ENOUGH_BANDWIDTH;
        case 454: return SESSION_NOT_FOUND;
        case 455: return METHOD_NOT_VALID;
        case 456: return HEADER_FIELD_NOT_VALID;
        case 457: return INVALID_RANGE;
        case 458: return PARAMETER_IS_READONLY;
        case 459: return AGGREGATE_OPERATION_NOT_ALLOWED;
        case 460: return ONLY_AGGREGATE_OPERATION_ALLOWED;
        case 461: return UNSUPPORTED_TRANSPORT;
        case 462: return DESTINATION_UNREACHABLE;
        case 463: return KEY_MANAGEMENT_FAILURE;
        case 505: return RTSP_VERSION_NOT_SUPPORTED;
        case 551: return OPTION_NOT_SUPPORTED;
        default:  return HTTPResponseStatus.valueOf(code);
        }
    }

    private RTSPResponseStatuses() {
    }
}
