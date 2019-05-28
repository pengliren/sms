package com.sms.server.net.rtsp.message;

import java.util.HashMap;
import java.util.Map;

import com.sms.server.net.http.message.HTTPMethod;

/**
 * RTSP Methods
 * @author pengliren
 *
 */
public final class RTSPMethods {

	/**
     * The OPTIONS method represents a request for information about the communication options available on the request/response
     * chain identified by the Request-URI. This method allows the client to determine the options and/or requirements
     * associated with a resource, or the capabilities of a server, without implying a resource action or initiating a
     * resource retrieval.
     */
    public static final HTTPMethod OPTIONS = HTTPMethod.OPTIONS;

    /**
     * The DESCRIBE method retrieves the description of a presentation or
     * media object identified by the request URL from a server.
     */
    public static final HTTPMethod DESCRIBE = new HTTPMethod("DESCRIBE");

    /**
     * The ANNOUNCE posts the description of a presentation or media object
     * identified by the request URL to a server, or updates the client-side
     * session description in real-time.
     */
    public static final HTTPMethod ANNOUNCE = new HTTPMethod("ANNOUNCE");

    /**
     * The SETUP request for a URI specifies the transport mechanism to be
     * used for the streamed media.
     */
    public static final HTTPMethod SETUP = new HTTPMethod("SETUP");

    /**
     * The PLAY method tells the server to start sending data via the
     * mechanism specified in SETUP.
     */
    public static final HTTPMethod PLAY = new HTTPMethod("PLAY");

    /**
     * The PAUSE request causes the stream delivery to be interrupted
     * (halted) temporarily.
     */
    public static final HTTPMethod PAUSE = new HTTPMethod("PAUSE");

    /**
     * The TEARDOWN request stops the stream delivery for the given URI,
     * freeing the resources associated with it.
     */
    public static final HTTPMethod TEARDOWN = new HTTPMethod("TEARDOWN");

    /**
     * The GET_PARAMETER request retrieves the value of a parameter of a
     * presentation or stream specified in the URI.
     */
    public static final HTTPMethod GET_PARAMETER = new HTTPMethod("GET_PARAMETER");

    /**
     * The SET_PARAMETER requests to set the value of a parameter for a
     * presentation or stream specified by the URI.
     */
    public static final HTTPMethod SET_PARAMETER = new HTTPMethod("SET_PARAMETER");

    /**
     * The REDIRECT request informs the client that it must connect to another
     * server location.
     */
    public static final HTTPMethod REDIRECT = new HTTPMethod("REDIRECT");

    /**
     * The RECORD method initiates recording a range of media data according to
     * the presentation description.
     */
    public static final HTTPMethod RECORD = new HTTPMethod("RECORD");

    private static final Map<String, HTTPMethod> methodMap = new HashMap<String, HTTPMethod>();

    static {
        methodMap.put(DESCRIBE.toString(), DESCRIBE);
        methodMap.put(ANNOUNCE.toString(), ANNOUNCE);
        methodMap.put(GET_PARAMETER.toString(), GET_PARAMETER);
        methodMap.put(OPTIONS.toString(), OPTIONS);
        methodMap.put(PAUSE.toString(), PAUSE);
        methodMap.put(PLAY.toString(), PLAY);
        methodMap.put(RECORD.toString(), RECORD);
        methodMap.put(REDIRECT.toString(), REDIRECT);
        methodMap.put(SETUP.toString(), SETUP);
        methodMap.put(SET_PARAMETER.toString(), SET_PARAMETER);
        methodMap.put(TEARDOWN.toString(), TEARDOWN);
    }

    /**
     * Returns the {@link HTTPMethod} represented by the specified name.
     * If the specified name is a standard RTSP method name, a cached instance
     * will be returned.  Otherwise, a new instance will be returned.
     */
    public static HTTPMethod valueOf(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        name = name.trim().toUpperCase();
        if (name.length() == 0) {
            throw new IllegalArgumentException("empty name");
        }

        HTTPMethod result = methodMap.get(name);
        if (result != null) {
            return result;
        } else {
            return new HTTPMethod(name);
        }
    }

    private RTSPMethods() {
    }
}
