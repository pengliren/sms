package com.sms.server.stream.timeshift;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.ITag;
import com.sms.io.flv.impl.FLVReader;
import com.sms.server.messaging.IMessage;
import com.sms.server.messaging.IMessageComponent;
import com.sms.server.messaging.IPassive;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.IPipeConnectionListener;
import com.sms.server.messaging.IPullableProvider;
import com.sms.server.messaging.OOBControlMessage;
import com.sms.server.messaging.PipeConnectionEvent;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.FlexStreamSend;
import com.sms.server.net.rtmp.event.IRTMPEvent;
import com.sms.server.net.rtmp.event.Invoke;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.event.Unknown;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.rtmp.message.Constants;
import com.sms.server.stream.ISeekableProvider;
import com.sms.server.stream.IStreamTypeAwareProvider;
import com.sms.server.stream.message.RTMPMessage;

/**
 * Timeshift Provider
 * @author pengliren
 * 
 */
public class TimeshiftingProvider implements IPassive, ISeekableProvider, IPullableProvider, IPipeConnectionListener,IStreamTypeAwareProvider{

	private static Logger log = LoggerFactory.getLogger(TimeshiftingProvider.class);
	
	private LinkedList<String> fileList;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private String streamPath = null;
	
	private long compareTime;
	
	private long startTime = 0;
	
	private long endTime = 0;
	
	private String lastFilename="";
	
	private IRecordFLVReader reader = null;
	
	private RTMPMessage videoConfigure = null;
	
	private RTMPMessage audioConfigure = null;
	
	private long offsetTs = 0;
	
	private int lastTimestamp = -1;
	
	private long currentTs = 0;
	
	private boolean inited = false;
	
	/**
	 * Consumer pipe
	 */
	private IPipe pipe;
	
	public TimeshiftingProvider(String storePath, String stream, final String timestamp) {
	
		fileList = new LinkedList<String>();
		
		streamPath = new StringBuilder(storePath).append(File.separator).append(stream).append(File.separator).append(timestamp.substring(0,8)).toString();
		log.debug("stream path:{}",streamPath);
		try {
			startTime = compareTime = sdf.parse(timestamp).getTime();
		} catch (ParseException e) {
			log.debug("timesstamp parse fail");
		}
	}
	
	private void init() {
		
		refreshFilelist();
		
		String firstFileName = fileList.pollFirst();
		if(StringUtils.isEmpty(firstFileName)) return;
		
		File firstFile = new File(streamPath, firstFileName);
		long firstFileTs = 0;
		try {
			reader = RecordFLVReaderCreator.createRecordFLVReader(firstFile);
			ITag tempTag;
			boolean videoConfChecked = false;
			boolean audioConfChecked = false;
			int i = 0;
			while(i < 10) {
				tempTag = reader.readTag();				
				if(tempTag.getDataType() == Constants.TYPE_VIDEO_DATA){
					videoConfChecked = true;
					if ((tempTag.getBody().get(0) & 0xFF) == 0x17 && tempTag.getBody().get(1) == (byte)0x00 ){
						videoConfigure = RTMPMessage.build(new VideoData(tempTag.getBody()) , 0);
					}
				} else if(tempTag.getDataType() == Constants.TYPE_AUDIO_DATA) {
					audioConfChecked = true;
					if ((tempTag.getBody().get(0) & 0xF0) == 0xA0 && tempTag.getBody().get(1) == (byte)0x00){						
						audioConfigure = RTMPMessage.build(new AudioData(tempTag.getBody()), 0);
					}
				} 
				
				if(videoConfChecked && audioConfChecked) break;
			}
			
			firstFileTs =  sdf.parse(firstFile.getName().substring(0,14)).getTime();
		} catch (Exception e) {
			log.info("exception {}", e.getMessage());
			if(hasMoreFile()){
				nextFile();				
			}
			return;
		} 				 
		int timeoffset = (int)(startTime - firstFileTs);
		reader.seekByTs(timeoffset / 1000);
	}
	
	private void refreshFilelist() {
		
		File streamDir = new File(streamPath);		
		File[] files =null ;
		if(streamDir.exists() && streamDir.isDirectory()){
			files= streamDir.listFiles(new FileFilter(){
				@Override
				public boolean accept(File pathname) {
					String filename = pathname.getName();
					if(!Pattern.matches("^\\d{14}.flv$", filename)){
						return false;
					}
					if(filename.compareTo(lastFilename)<1) return false;
					try {
						long start = sdf.parse(filename.substring(0, 14)).getTime();
						long end = start + FLVReader.getDuration(pathname);
						if(end < compareTime) return false;
						if(end > endTime) endTime = end;
					} catch (ParseException e) {
						log.info("refreshFilelist parse file fail");
						return false;
					}
					return true;
				}});
		}
		
		if(files != null && files.length>0 ){
			String[] filepaths = new String[files.length];
			for(int i = 0; i < files.length; i++) filepaths[i] = files[i].getName();
			Arrays.sort(filepaths);
			for(int i = 0; i < filepaths.length; i++){
				fileList.add(filepaths[i]);
			}
			compareTime = endTime;
			lastFilename = filepaths[filepaths.length-1];
		}
	}
	
	private void nextFile() {
		if(reader != null) reader.close();
		reader = null;
		lastTimestamp=-1;
		offsetTs = currentTs;
		String nextFileName = fileList.pollFirst();
		try {
			reader = RecordFLVReaderCreator.createRecordFLVReader(new File(streamPath, nextFileName));
			reader.seekByTs(0);			
		} catch (Exception e) {
			log.info("next file exception");
			reader.close();
		}		
	}
	
	private boolean hasMoreFile(){
		
		refreshFilelist();
		return fileList.size()>0;
	}
	
	@Override
	public void onOOBControlMessage(IMessageComponent source, IPipe pipe, OOBControlMessage oobCtrlMsg) {
		
		String serviceName = oobCtrlMsg.getServiceName();
    	String target = oobCtrlMsg.getTarget();
    	log.debug("onOOBControlMessage - service name: {} target: {}", serviceName, target);
    	if (serviceName != null) {
    		if (IPassive.KEY.equals(target)) {
    			if ("init".equals(serviceName)) {
    				
    			}
    		} else if (ISeekableProvider.KEY.equals(target)) {
    			if ("seek".equals(serviceName)) {
    				
    			}
    		} else if (IStreamTypeAwareProvider.KEY.equals(target)) {
    			if ("hasVideo".equals(serviceName)) {
    				oobCtrlMsg.setResult(true);
    			}
    		}
    	}
	}

	@Override
	public boolean hasVideo() {

		return true;
	}

	@Override
	public void onPipeConnectionEvent(PipeConnectionEvent event) {
		switch (event.getType()) {
		case PipeConnectionEvent.PROVIDER_CONNECT_PULL:
			if (pipe == null) {
				pipe = (IPipe) event.getSource();
			}
			break;
		case PipeConnectionEvent.PROVIDER_DISCONNECT:
			if (pipe == event.getSource()) {
				this.pipe = null;
				reader.close();
			}
			break;
		case PipeConnectionEvent.CONSUMER_DISCONNECT:
			if (pipe == event.getSource()) {
				reader.close();
			}
		default:
		}
	}

	@Override
	public IMessage pullMessage(IPipe pipe) throws IOException {

		if(!inited){
			init();
			inited=true;
		}
		if(reader == null) return null;
		RTMPMessage msg ;
		if(videoConfigure != null){
			msg = videoConfigure;
			videoConfigure = null;
			log.debug("send video configure:{}",msg);
			return msg;
		}
		if(audioConfigure != null){
			msg = audioConfigure;
			log.debug("send audio configure:{}",msg);
			audioConfigure = null;
			return msg;
		}
		IRTMPEvent event;
		ITag tag = null;
		if (reader.hasMoreTags()) {
			tag = reader.readTag();
			if(lastTimestamp==-1) lastTimestamp=tag.getTimestamp();
			currentTs = offsetTs + (tag.getTimestamp() - lastTimestamp);
			switch (tag.getDataType()) {
				case Constants.TYPE_AUDIO_DATA:
					event = new AudioData(tag.getBody());
					break;
				case Constants.TYPE_VIDEO_DATA:
					event = new VideoData(tag.getBody());
					break;
				case Constants.TYPE_INVOKE:
					event = new Invoke(tag.getBody());
					break;
				case Constants.TYPE_NOTIFY:
					event = new Notify(tag.getBody());
					break;
				case Constants.TYPE_FLEX_STREAM_SEND:
					event = new FlexStreamSend(tag.getBody());
					break;
				default:
					log.warn("Unexpected type? {}", tag.getDataType());
					event = new Unknown(tag.getDataType(), tag.getBody());
			}
			event.setTimestamp(currentTs);			
			return RTMPMessage.build(event, currentTs);			
		} else {
			if (hasMoreFile()) {
				nextFile();
				return pullMessage(pipe);
			} else {
				pipe.unsubscribe(this);
				return null;
			}			
		}
	}

	@Override
	public IMessage pullMessage(IPipe pipe, long wait) throws IOException {

		return pullMessage(pipe);
	}

	@Override
	public int seek(int ts) {

		return 0;
	}

}
