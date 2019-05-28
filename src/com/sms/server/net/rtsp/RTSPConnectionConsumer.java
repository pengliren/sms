package com.sms.server.net.rtsp;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IConnection;
import com.sms.server.api.stream.IStreamListener;
import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.messaging.IMessage;
import com.sms.server.messaging.IMessageComponent;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.OOBControlMessage;
import com.sms.server.net.http.stream.ICustomPushableConsumer;
import com.sms.server.net.rtmp.status.StatusCodes;
import com.sms.server.stream.message.RTMPMessage;
import com.sms.server.stream.message.StatusMessage;

/**
 * RTSP Conection Consumer
 * @author penglrien
 *
 */
public class RTSPConnectionConsumer implements ICustomPushableConsumer {
	
	private static Logger log = LoggerFactory.getLogger(RTSPConnectionConsumer.class);
	
	private boolean closed = false;
	
	private List<IStreamListener> listeners;
	
	private RTSPMinaConnection conn;	
	
	public RTSPConnectionConsumer(RTSPMinaConnection conn) {
	
		this.conn = conn;
		listeners = new CopyOnWriteArrayList<IStreamListener>();
	}
	
	@Override
	public void pushMessage(IPipe pipe, IMessage message) throws IOException {
		
		if (message instanceof RTMPMessage) {
			if (((RTMPMessage) message).getBody() instanceof IStreamPacket) {				
				IStreamPacket packet = (IStreamPacket) (((RTMPMessage) message).getBody());
				if (packet.getData() != null) {					
					for(IStreamListener listener : listeners) {						
						listener.packetReceived(null, packet);
					}
				}
			}
		} else if(message instanceof StatusMessage) {
			if(((StatusMessage) message).getBody().getCode().equals(StatusCodes.NS_PLAY_UNPUBLISHNOTIFY)) {
				closed = true;
				conn.close();
			}
		}
	}

	@Override
	public void onOOBControlMessage(IMessageComponent source, IPipe pipe,
			OOBControlMessage oobCtrlMsg) {
		
	}

	@Override
	public IConnection getConnection() {
		
		return conn;
	}

	public boolean isClosed() {
		return closed;
	}
	
	public void addStreamListener(IStreamListener listener) {
		
		log.info("add a stream listener");
		this.listeners.add(listener);
	}
	
	public void removeStreamListener(IStreamListener listener) {
		
		if(listener != null) {
			log.info("remove a stream listener");
			this.listeners.remove(listener);
		}
	}
}
