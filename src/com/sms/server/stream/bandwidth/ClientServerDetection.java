package com.sms.server.stream.bandwidth;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IConnection;
import com.sms.server.api.SMS;
import com.sms.server.api.service.IPendingServiceCall;
import com.sms.server.api.service.IPendingServiceCallback;
import com.sms.server.api.stream.IStreamCapableConnection;

public class ClientServerDetection implements IPendingServiceCallback {

	protected static Logger log = LoggerFactory.getLogger(ClientServerDetection.class);

	public ClientServerDetection() {

	}

	/**
	 * Handle callback from service call.
	 */

	public void resultReceived(IPendingServiceCall call) {

	}

	private IStreamCapableConnection getStats() {
		IConnection conn = SMS.getConnectionLocal();
		if (conn instanceof IStreamCapableConnection) {
			return (IStreamCapableConnection) conn;
		}
		return null;
	}

	public Map<String, Object> checkBandwidth(Object[] params) {
		final IStreamCapableConnection stats = this.getStats();

		Map<String, Object> statsValues = new HashMap<String, Object>();
		Integer time = (Integer) (params.length > 0 ? params[0] : 0);
		statsValues.put("cOutBytes", stats.getReadBytes());
		statsValues.put("cInBytes", stats.getWrittenBytes());
		statsValues.put("time", time);
		
		log.debug("cOutBytes: {} cInBytes: {} time: {}", new Object[]{stats.getReadBytes(), stats.getWrittenBytes(), time});

		return statsValues;

	}
}
