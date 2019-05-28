package com.sms.server.net.http;

import java.util.Collection;

public interface IHTTPConnManager {

	HTTPMinaConnection getConnection(long clientId);

	void addConnection(HTTPMinaConnection conn, long clientId);

	HTTPMinaConnection removeConnection(long clientId);

	Collection<HTTPMinaConnection> removeConnections();
	
	int getConnectionCount();
}
