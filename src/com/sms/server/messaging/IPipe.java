package com.sms.server.messaging;


/**
 * A pipe is an object that connects message providers and
 * message consumers. Its main function is to transport messages
 * in kind of ways it provides.
 *
 * Pipes fire events as they go, these events are common way to work with pipes for
 * higher level parts of server.
 */
public interface IPipe extends IMessageInput, IMessageOutput {
    /**
     * Add connection event listener to pipe
     * @param listener          Connection event listener
     */
    void addPipeConnectionListener(IPipeConnectionListener listener);

    /**
     * Add connection event listener to pipe
     * @param listener          Connection event listener
     */
	void removePipeConnectionListener(IPipeConnectionListener listener);
}
