package com.sms.server.stream.bandwidth;

import com.sms.server.api.IConnection;

public interface IBandwidthDetection {
	public void checkBandwidth(IConnection p_client);
	public void calculateClientBw(IConnection p_client);
}
