package com.sms.server.stream.timeshift;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.ITag;
import com.sms.io.amf.Output;
import com.sms.io.flv.impl.Tag;
import com.sms.server.ContextBean;
import com.sms.server.ScopeContextBean;
import com.sms.server.api.IScope;
import com.sms.server.api.event.IEvent;
import com.sms.server.api.stream.IBroadcastStream;
import com.sms.server.api.stream.IStreamListener;
import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.rtmp.message.Constants;
import com.sms.server.stream.ClientBroadcastStream;
import com.sms.server.util.SystemTimer;

/**
 * Live Broadcast Stream Record
 * @author pengliren
 *
 */
public class RecordableBroadcastStream extends ClientBroadcastStream implements IStreamListener{

	private Logger log = LoggerFactory.getLogger(RecordableBroadcastStream.class);
	
	private String storePath;
	
	private boolean canRecord = false;
	
	private long durationPerFile = 1000*60*15;//默认15分钟
	
	private long lastRecordTime=-1;
	
	private long lastStreamTime = -1;
	
	private long currentStreamTime = -1;
	
	private RecordFLVWriter writer;
	
	private long lastTimecode = -1;
	
	public RecordableBroadcastStream() {
		
		addStreamListener(this);
	}
	
	@Override
	public void setScope(IScope scope) {
		
		super.setScope(scope);
		ContextBean ctxBean = getScope().getContext().getScopeCtxBean().getClazz(ScopeContextBean.BROADCASTSTREAM_BEAN); 
		storePath = ctxBean.getProperty("path");
		canRecord = Boolean.parseBoolean(ctxBean.getProperty("record"));
		if (!StringUtils.isEmpty(ctxBean.getProperty("duration"))) {
			durationPerFile = Long.parseLong(ctxBean.getProperty("duration")) * 1000;
		}
	}
	
	private void startNewWriter() {
		
		closeWriter();
		String path = genRecordPath();
		File recordFile = new File(path);
		try {
			if(!recordFile.getParentFile().exists()) { 
				recordFile.getParentFile().mkdirs();	
				if(File.separatorChar=='/')
					Runtime.getRuntime().exec(String.format("chmod 777 %s", recordFile.getParentFile().getCanonicalPath()));
			}
			
			if(File.separatorChar=='/')			
					Runtime.getRuntime().exec(String.format("chmod 777 %s", recordFile.getCanonicalPath()));
		} catch (IOException e) {			
			log.info("exception {}", e.getMessage());
		}
	    writer = new RecordFLVWriter(recordFile, false);
		
		lastRecordTime = SystemTimer.currentTimeMillis();
		lastStreamTime = currentStreamTime;
		if(this.getCodecInfo()!=null){
			if(this.getCodecInfo().getVideoCodecName()!=null && this.getCodecInfo().getVideoCodecName().equals("AVC")){
				writePacket(0, new VideoData(this.getCodecInfo().getVideoCodec().getDecoderConfiguration()));
			}
			if(this.getCodecInfo().getAudioCodecName()!=null && this.getCodecInfo().getAudioCodecName().equals("AAC")){				
				writePacket(0, new AudioData(this.getCodecInfo().getAudioCodec().getDecoderConfiguration()));
			}
		}
	}
	
	private void writePacket(int timestamp,IStreamPacket packet){
		ITag tag = new Tag();
		tag.setDataType(packet.getDataType());
		tag.setTimestamp(timestamp);
		tag.setBodySize(packet.getData().limit());
		tag.setBody(packet.getData().asReadOnlyBuffer());		
		writer.putTag(tag);	
	}
	
	private void closeWriter() {
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}
	
	public String getStorePath(){
		if(storePath == null){			
			storePath = this.getScope().getContext().getContextPath()+"/streams";		
		}
		return storePath;
	}
	
	private String genRecordPath(){
		
		StringBuilder sb = new StringBuilder(getStorePath());
		if(!storePath.endsWith("/") && !storePath.endsWith("\\")) sb.append(File.separatorChar);
		sb.append(this.getPublishedName());
		sb.append(File.separatorChar);
		Date date = new Date();
		sb.append(new SimpleDateFormat("yyyyMMdd").format(date));
	    sb.append(File.separatorChar);
	    sb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(date)).append(".flv");
		
		return sb.toString();
	}
	
	@Override
	public void dispatchEvent(IEvent event) {
		super.dispatchEvent(event);
		long current = SystemTimer.currentTimeMillis();
		if (current - lastTimecode >= 1000) {
			Notify timecodeNotify = new Notify();
			timecodeNotify.setInvokeId(-100);
			timecodeNotify.setTimestamp(this.getCurrentTimestamp());
			IoBuffer timecodeBuff = IoBuffer.allocate(100);
			Output out = new Output(timecodeBuff);
			out.writeString("onTimecode");
			out.writeDate(new Date(current));
			timecodeBuff.flip();
			timecodeNotify.setData(timecodeBuff);
			super.dispatchEvent(timecodeNotify);
			lastTimecode = current;
		}
	}

	@Override
	public void packetReceived(IBroadcastStream stream, IStreamPacket packet) {
		
		if(!canRecord) return;
		if(!isAvailable(packet)) return;
		currentStreamTime = packet.getTimestamp();
		if(lastStreamTime==-1) lastStreamTime = currentStreamTime;
		if (writer == null || ((SystemTimer.currentTimeMillis() - lastRecordTime) > durationPerFile) && isKeyPacket(packet)) startNewWriter();
		writePacket((int) (currentStreamTime - lastStreamTime), packet);
	}
	
	private boolean isAvailable(IStreamPacket packet) {
		if (packet.getDataType() == Constants.TYPE_VIDEO_DATA) {
			if (packet.getData().get(0) == (byte) 0x17 && packet.getData().get(1) == (byte) 0x00)
				return false;
			return true;
		} else if (packet.getDataType() == Constants.TYPE_AUDIO_DATA) {
			if ((packet.getData().get(0) & 0xf0) == 0xa0 && packet.getData().get(1) == (byte) 0x00)
				return false;
			return true;
		} else if (packet.getDataType() == Constants.TYPE_NOTIFY) {
			if (writer == null)
				return false;
			return true;
		}
		return false;
	}
	
	private boolean isKeyPacket(IStreamPacket packet) {
		if (packet.getDataType() == Constants.TYPE_VIDEO_DATA && (packet.getData().get(0) & 0xf0) == 0x10)
			return true;
		return false;
	}

	public boolean isCanRecord() {
		return canRecord;
	}

	public void setCanRecord(boolean canRecord) {
		this.canRecord = canRecord;
		if(!canRecord) {
			closeWriter();
		}
	}
}
