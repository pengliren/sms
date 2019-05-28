package com.sms.server.net.http.message;

/**
 * HTTP Request Interface
 * @author pengliren
 *
 */
public interface HTTPRequest extends HTTPMessage {

    /**
     * Returns the method of this request.
     */
    HTTPMethod getMethod();

    /**
     * Sets the method of this request.
     */
    void setMethod(HTTPMethod method);

    /**
     * Returns the URI (or path) of this request.
     */
    String getUri();

    /**
     * Sets the URI (or path) of this request.
     */
    void setUri(String uri);
    
    /**
     * Returns the path of this request.
     */
    String getPath();
    
    /**
     * Returns the path of this request.
     */
    void setPath(String path);
}
