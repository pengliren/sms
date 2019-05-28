package com.sms.server.api.service;

import com.sms.server.api.IConnection;

/**
 * Connection that has options to invoke and handle remote calls
 */
// TODO: this should really extend IServiceInvoker
public interface IServiceCapableConnection extends IConnection {
    /**
     * Invokes service using remoting call object
     * @param call       Service call object
     */
    void invoke(IServiceCall call);

    /**
     * Invoke service using call and channel
     * @param call       Service call
     * @param channel    Channel used
     */
    void invoke(IServiceCall call, int channel);

    /**
     * Invoke method by name
     * @param method     Called method name
     */
    void invoke(String method);

    /**
     * Invoke method by name with callback
     * @param method     Called method name
     * @param callback   Callback
     */
    void invoke(String method, IPendingServiceCallback callback);

    /**
     * Invoke method with parameters
     * @param method     Method name
     * @param params     Invocation parameters passed to method
     */
    void invoke(String method, Object[] params);

    /**
     *
     * @param method
     * @param params
     * @param callback
     */
    void invoke(String method, Object[] params,
			IPendingServiceCallback callback);

    /**
     *
     * @param call
     */
    void notify(IServiceCall call);

    /**
     *
     * @param call
     * @param channel
     */
    void notify(IServiceCall call, int channel);

    /**
     *
     * @param method
     */
    void notify(String method);

    /**
     * 
     * @param method
     * @param params
     */
    void notify(String method, Object[] params);

}
