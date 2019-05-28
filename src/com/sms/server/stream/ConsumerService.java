package com.sms.server.stream;

import com.sms.server.api.stream.IClientStream;
import com.sms.server.api.stream.IStreamCapableConnection;
import com.sms.server.messaging.IMessageOutput;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.InMemoryPushPushPipe;
import com.sms.server.net.rtmp.RTMPConnection;
import com.sms.server.stream.consumer.ConnectionConsumer;

/**
 * Basic consumer service implementation. Used to get pushed messages at consumer endpoint.
 */
public class ConsumerService implements IConsumerService {

	private static final class SingletonHolder {

		private static final ConsumerService INSTANCE = new ConsumerService();
	}

	protected ConsumerService() {
		
	}
	
	public static ConsumerService getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
	/** {@inheritDoc} */
    public IMessageOutput getConsumerOutput(IClientStream stream) {
		IStreamCapableConnection streamConn = stream.getConnection();
		if (streamConn == null || !(streamConn instanceof RTMPConnection)) {
			return null;
		}
		RTMPConnection conn = (RTMPConnection) streamConn;
		// TODO Better manage channels.
		// now we use OutputStream as a channel wrapper.
		OutputStream o = conn.createOutputStream(stream.getStreamId());
		IPipe pipe = new InMemoryPushPushPipe();
		pipe.subscribe(new ConnectionConsumer(conn, o.getVideo().getId(), o.getAudio().getId(), o.getData().getId()), null);
		return pipe;
	}

}
