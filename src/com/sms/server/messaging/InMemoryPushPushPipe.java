package com.sms.server.messaging;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple in-memory version of push-push pipe.
 * It is triggered by an active provider to push messages
 * through it to an event-driven consumer.
 */
public class InMemoryPushPushPipe extends AbstractPipe {
	
	private static final Logger log = LoggerFactory.getLogger(InMemoryPushPushPipe.class);

	/** {@inheritDoc} */
	@Override
	public boolean subscribe(IConsumer consumer, Map<String, Object> paramMap) {
		if (consumer instanceof IPushableConsumer) {
			boolean success = super.subscribe(consumer, paramMap);
			if (success) {
				fireConsumerConnectionEvent(consumer, PipeConnectionEvent.CONSUMER_CONNECT_PUSH, paramMap);
			}
			return success;
		} else {
			throw new IllegalArgumentException("Non-pushable consumer not supported by PushPushPipe");
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean subscribe(IProvider provider, Map<String, Object> paramMap) {
		boolean success = super.subscribe(provider, paramMap);
		if (success) {
			fireProviderConnectionEvent(provider, PipeConnectionEvent.PROVIDER_CONNECT_PUSH, paramMap);
		}
		return success;
	}

	/** {@inheritDoc} */
	public IMessage pullMessage() {
		return null;
	}

	/** {@inheritDoc} */
	public IMessage pullMessage(long wait) {
		return null;
	}

	/**
	 * Pushes a message out to all the PushableConsumers.
	 * 
	 * @param message the message to be pushed to consumers.
	 */
	public void pushMessage(IMessage message) throws IOException {
	
		for (IConsumer consumer : consumers) {
			try {
				IPushableConsumer pcon = (IPushableConsumer) consumer;
				pcon.pushMessage(this, message);
			} catch (Throwable t) {
				if (t instanceof IOException) {
					// Pass this along
					throw (IOException) t;
				}
				log.error("Exception when pushing message to consumer", t);
			}
		}
	}
}
