package com.sms.server.stream.proxy;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.ITag;
import com.sms.io.IoConstants;
import com.sms.io.flv.impl.Tag;
import com.sms.io.utils.IOUtils;
import com.sms.server.api.service.IPendingServiceCall;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.util.SystemTimer;

/**
 * HTTP FLV代理流
 * @author pengliren
 *
 */
public abstract class HTTPProxyStream extends BaseRTMPProxyStream implements IoConstants {

	private static Logger log = LoggerFactory.getLogger(HTTPProxyStream.class);

	private IoBuffer lastBuffer;
	
	private volatile boolean skipFlvHeader = false;
	
	public HTTPProxyStream(String streamName) {
	
		super();
		setPublishedName(streamName);	
	}
		
	@Override
	public void stop() {
		
		synchronized (lock) {
			super.stop();
			start = false;
			getConnection().close();
			connManager.unregister(publishedName);
		}
	}

	@Override
	public void resultReceived(IPendingServiceCall call) {
		
		log.info("http proxy handle call result:{}",call);
	}
	
	private synchronized void dispatchEvent(ITag tag) {
		
		byte dataType = tag.getDataType();
		if(dataType == TYPE_METADATA) {
			
			Notify notify = new Notify();
			notify.setTimestamp(tag.getTimestamp());
			notify.setData(tag.getBody());	
			notify.setSource(getConnection());
			dispatchEvent(notify);
		} else if(dataType == TYPE_AUDIO) {
			
			AudioData aData = new AudioData();
			aData.setData(tag.getBody());
			aData.setTimestamp(tag.getTimestamp());
			aData.setSource(getConnection());
			dispatchEvent(aData);	
		} else if(dataType == TYPE_VIDEO) {
			
			VideoData vData = new VideoData();
			vData.setData(tag.getBody());
			vData.setTimestamp(tag.getTimestamp());
			vData.setSource(getConnection());
			dispatchEvent(vData);			
		}
	}
	
	public void handleMessage(IoBuffer in) throws Exception {
		
		lastReceiveTime = SystemTimer.currentTimeMillis();
		if(in == null || in.remaining() == 0) return;
		if(lastBuffer == null) {
			lastBuffer = in;
		} else {			
			lastBuffer.put(in);
			lastBuffer.flip();
		}
		
		if(!skipFlvHeader) {
			if(lastBuffer.remaining() > 9) {
				byte[] skipByte = new byte[9];
				lastBuffer.get(skipByte);
				skipFlvHeader = true;
			} else {
				resetLastBuffer();
				return;
			}
		}
		
		byte[] data = null;
		while(lastBuffer.remaining() > 15) {
			lastBuffer.mark();
			ITag tag = readTagHeader(lastBuffer);
			if(tag != null && lastBuffer.remaining() > tag.getBodySize()) {
				data = new byte[tag.getBodySize()];				
				lastBuffer.get(data);
				tag.setBody(IoBuffer.wrap(data, 0, tag.getBodySize()));
				IoBuffer temp = lastBuffer.slice();
				lastBuffer = IoBuffer.allocate(4096).setAutoExpand(true);
				lastBuffer.put(temp);
				lastBuffer.flip();
				dispatchEvent(tag);
				temp = null;
			} else {
				lastBuffer.reset();
				break;
			}
		}
		
		resetLastBuffer();
	}
	
	private void resetLastBuffer() {
		if(lastBuffer.remaining() > 0) {
			IoBuffer temp = IoBuffer.allocate(lastBuffer.limit()).setAutoExpand(true);
			temp.put(lastBuffer);
			lastBuffer = temp;
		} else {
			lastBuffer = null;
		}
	}
	
	private ITag readTagHeader(IoBuffer in) {
		
		// previous tag size (4 bytes) + flv tag header size (11 bytes)
		//		if (log.isDebugEnabled()) {
		//			in.mark();
		//			StringBuilder sb = new StringBuilder();
		//			HexDump.dumpHex(sb, in.array());
		//			log.debug("\n{}", sb);
		//			in.reset();
		//		}		
		// previous tag's size
		int previousTagSize = in.getInt();
		// start of the flv tag
		byte dataType = in.get();
		// loop counter
		int i = 0;
		while (dataType != 8 && dataType != 9 && dataType != 18) {
			log.info("Invalid data type detected, reading ahead");
			// only allow 10 loops
			if (i++ > 10) {
				return null;
			}
			// move ahead and see if we get a valid datatype		
			dataType = in.get();
		}
		int bodySize = IOUtils.readUnsignedMediumInt(in);
		int timestamp = IOUtils.readExtendedMediumInt(in);
		in.skip(3);
		return new Tag(dataType, timestamp, bodySize, null, previousTagSize);
	}
	
}
