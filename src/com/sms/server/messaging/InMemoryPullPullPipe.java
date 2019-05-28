package com.sms.server.messaging;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple in-memory version of pull-pull pipe.
 * It is triggered by an active consumer that pulls messages
 * through it from a pullable provider.
 */
public class InMemoryPullPullPipe extends AbstractPipe {
	private static final Logger log = LoggerFactory.getLogger(InMemoryPullPullPipe.class);

	/** {@inheritDoc} */
	@Override
	public boolean subscribe(IConsumer consumer, Map<String, Object> paramMap) {
		boolean success = super.subscribe(consumer, paramMap);
		if (success) {
			fireConsumerConnectionEvent(consumer, PipeConnectionEvent.CONSUMER_CONNECT_PULL, paramMap);
		}
		return success;
	}

	/** {@inheritDoc} */
	@Override
	public boolean subscribe(IProvider provider, Map<String, Object> paramMap) {
		if (provider instanceof IPullableProvider) {
			boolean success = super.subscribe(provider, paramMap);
			if (success) {
				fireProviderConnectionEvent(provider, PipeConnectionEvent.PROVIDER_CONNECT_PULL, paramMap);
			}
			return success;		
		} else {
			throw new IllegalArgumentException("Non-pullable provider not supported by PullPullPipe");
		}
	}

	/** {@inheritDoc} */
	public IMessage pullMessage() throws IOException {
		IMessage message = null;
		for (IProvider provider : providers) {
			if (provider instanceof IPullableProvider) {
				// choose the first available provider
				try {
					message = ((IPullableProvider) provider).pullMessage(this);
					if (message != null) {
						break;
					}
				} catch (Throwable t) {
					if (t instanceof IOException) {
						// Pass this along
						throw (IOException) t;
					}
					log.error("exception when pulling message from provider", t.getMessage());
				}
			}
		}
		return message;
	}

	/** {@inheritDoc} */
	public IMessage pullMessage(long wait) {
		IMessage message = null;
		// divided evenly
		int size = providers.size();
		long averageWait = size > 0 ? wait / size : 0;
		// choose the first available provider
		for (IProvider provider : providers) {
			if (provider instanceof IPullableProvider) {
    			try {
    				message = ((IPullableProvider) provider).pullMessage(this, averageWait);
    				if (message != null) {
    					break;
    				}
    			} catch (Throwable t) {
    				log.error("exception when pulling message from provider", t);
    			}
			}
		}
		return message;
	}

	/** {@inheritDoc} */
	public void pushMessage(IMessage message) {
		// push mode ignored
	}

}
