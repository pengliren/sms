package com.sms.server.net.rtmp;

import com.sms.server.net.rtmp.event.Notify;

public interface INetStreamEventHandler {
	void onStreamEvent(Notify notify);
}
