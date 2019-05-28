package com.sms.server.messaging;

/**
 * A listener that wants to listen to events when
 * provider/consumer connects to or disconnects from
 * a specific pipe.
 */
public interface IPipeConnectionListener {
    /**
     * Pipe connection event handler
     * @param event        Pipe connection event
     */
    void onPipeConnectionEvent(PipeConnectionEvent event);
}
