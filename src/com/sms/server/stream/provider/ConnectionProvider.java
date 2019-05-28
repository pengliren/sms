package com.sms.server.stream.provider;

import com.sms.server.messaging.IMessageComponent;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.IPipeConnectionListener;
import com.sms.server.messaging.IProvider;
import com.sms.server.messaging.OOBControlMessage;
import com.sms.server.messaging.PipeConnectionEvent;

/**
 * Provides connection via pipe
 */
public class ConnectionProvider implements IProvider, IPipeConnectionListener {
    /**
     * Pipe used by connection
     */
    private IPipe pipe;

	/** {@inheritDoc} */
	public void onOOBControlMessage(IMessageComponent source, IPipe pipe, OOBControlMessage oobCtrlMsg) {
	}

	/** {@inheritDoc} */
    public void onPipeConnectionEvent(PipeConnectionEvent event) {
		switch (event.getType()) {
			case PipeConnectionEvent.PROVIDER_CONNECT_PUSH:
				if (event.getProvider() == this) {
					this.pipe = (IPipe) event.getSource();
				}
				break;
			case PipeConnectionEvent.PROVIDER_DISCONNECT:
				if (this.pipe == event.getSource()) {
					this.pipe = null;
				}
				break;
			default:
				break;
		}
	}

}
