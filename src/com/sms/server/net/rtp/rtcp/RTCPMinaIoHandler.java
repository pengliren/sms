package com.sms.server.net.rtp.rtcp;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTCPMinaIoHandler extends IoHandlerAdapter {

	private static Logger log = LoggerFactory.getLogger(RTCPMinaIoHandler.class);
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		
		cause.printStackTrace();
		log.info("rtcp exception {}", cause.getMessage());
		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {

		/*IoBuffer in = (IoBuffer)message;
		InetSocketAddress remoteAddress = (InetSocketAddress)session.getRemoteAddress();
		String key = String.format("%s:%d", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
		RTPPlayer player = RTSPCore.rtpSocketMaps.get(key);
		if(rtpSocket != null) {
			
		}*/
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {

		log.info("rtcp session close");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {

		log.info("rtcp session create");
	}
}
