package com.sms.server.net.http.stream;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.utils.IOUtils;
import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.messaging.IMessage;
import com.sms.server.messaging.IMessageComponent;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.OOBControlMessage;
import com.sms.server.net.http.HTTPMinaConnection;
import com.sms.server.net.rtmp.status.StatusCodes;
import com.sms.server.stream.message.RTMPMessage;
import com.sms.server.stream.message.StatusMessage;

public class HTTPConnectionConsumer implements ICustomPushableConsumer {

    private static final Logger log = LoggerFactory.getLogger(HTTPConnectionConsumer.class);
	
	private HTTPMinaConnection conn;
	
	private boolean closed = false;
	
	private boolean inited = false;
	
	private static IoBuffer header = IoBuffer.allocate(13);
	
	static {
		// write flv header
    	header.put("FLV".getBytes());
    	header.put(new byte[] { 0x01, 0x05 });
    	header.putInt(0x09);
    	header.putInt(0x00);
    	header.flip();
	}
    
    public HTTPConnectionConsumer(HTTPMinaConnection conn) {
		
    	this.conn = conn;    		
	}
    
	@Override
	public void onOOBControlMessage(IMessageComponent source, IPipe pipe,
			OOBControlMessage oobCtrlMsg) {
		if ("ConnectionConsumer".equals(oobCtrlMsg.getTarget())) {
			if ("pendingVideoCount".equals(oobCtrlMsg.getServiceName())) {
				long pendings = conn.getPendingMessages();
				if(pendings > 500){
					log.info("http pending packet:{}", pendings);
					oobCtrlMsg.setResult(pendings);
				} else if(pendings > 1000) {
					log.info("http pending packet > 1000, network is bad");
					closed = true;
				}			
			}
		}
	}

	@Override
	public void pushMessage(IPipe pipe, IMessage message) {
		
		if(!inited) {
			conn.write(header.asReadOnlyBuffer());
			inited = true;
		}
		
		if (message instanceof RTMPMessage) {
			if (((RTMPMessage) message).getBody() instanceof IStreamPacket) {
				IStreamPacket packet = (IStreamPacket) (((RTMPMessage) message).getBody());
				if (packet.getData() != null) {
					int bodySize = packet.getData().limit();
					IoBuffer data = IoBuffer.allocate(bodySize+16);
					data.put(packet.getDataType());
					IOUtils.writeMediumInt(data, bodySize);
					IOUtils.writeExtendedMediumInt(data, (int)packet.getTimestamp());
					IOUtils.writeMediumInt(data, 0);
					data.put(packet.getData().duplicate());
					data.putInt(bodySize + 11);
					data.flip();
					
					conn.write(data);
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
	public HTTPMinaConnection getConnection() {

		return conn;
	}

	public boolean isClosed() {
		return closed;
	}
	
	public void setClose(boolean value) {
		
		this.closed = value;
	}
}
