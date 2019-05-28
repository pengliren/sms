package com.sms.server.net.http.stream;

import com.sms.server.api.IConnection;
import com.sms.server.messaging.IPushableConsumer;

public interface ICustomPushableConsumer extends IPushableConsumer {

	IConnection getConnection();
}
