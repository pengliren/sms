package com.sms.server.net.http.stream;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.ts.FLV2MPEGTSChunkWriter;
import com.sms.io.utils.HexDump;
import com.sms.server.Configuration;
import com.sms.server.api.IScope;
import com.sms.server.api.stream.IAudioStreamCodec;
import com.sms.server.api.stream.IBroadcastStream;
import com.sms.server.api.stream.IStreamCodecInfo;
import com.sms.server.api.stream.IStreamListener;
import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.api.stream.IVideoStreamCodec;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.IRTMPEvent;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.rtmp.event.VideoData.FrameType;

/**
 * FLV TO Mpeg2ts Segmenter
 * @author pengliren
 *
 */
public class MpegtsSegmenterService implements IStreamListener {

	private static Logger log = LoggerFactory.getLogger(MpegtsSegmenterService.class);
	
	private static ConcurrentMap<String, ConcurrentHashMap<String, SegmentFacade>> scopeSegMap = new ConcurrentHashMap<String, ConcurrentHashMap<String,SegmentFacade>>();
	
	// length of a segment in milliseconds
	private long segmentTimeLimit = Configuration.HLS_SEGMENT_TIME * 1000;
	
	// maximum number of segments to keep available per stream
	private int maxSegmentsPerFacade = Configuration.HLS_SEGMENT_MAX;
	
	private static final class SingletonHolder {

		private static final MpegtsSegmenterService INSTANCE = new MpegtsSegmenterService();
	}
	
	protected MpegtsSegmenterService() {
		
	}
	
	public static MpegtsSegmenterService getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
	public long getSegmentTimeLimit() {
		return segmentTimeLimit;
	}

	public void setSegmentTimeLimit(long segmentTimeLimit) {
		this.segmentTimeLimit = segmentTimeLimit;
	}

	public int getMaxSegmentsPerFacade() {
		return maxSegmentsPerFacade;
	}

	public void setMaxSegmentsPerFacade(int maxSegmentsPerFacade) {
		this.maxSegmentsPerFacade = maxSegmentsPerFacade;
	}
	
	public int getSegmentCount(String scopeName, String streamName) {
		ConcurrentHashMap<String, SegmentFacade> segments = scopeSegMap.get(scopeName);
		if (segments.containsKey(streamName)) {
			SegmentFacade facade = segments.get(streamName);
			return facade.getSegmentCount();
		} else {
			return 0;
		}   	
    }	
	
	public MpegtsSegment getSegment(String scopeName, String streamName) {
		ConcurrentHashMap<String, SegmentFacade> segments = scopeSegMap.get(scopeName);
		if (segments.containsKey(streamName)) {
			SegmentFacade facade = segments.get(streamName);
			return facade.getSegment();
		} else {
			return null;
		}
	}
	
	public MpegtsSegment getSegment(String scopeName, String streamName, int index) {
		ConcurrentHashMap<String, SegmentFacade> segments = scopeSegMap.get(scopeName);
		if (segments.containsKey(streamName)) {
			SegmentFacade facade = segments.get(streamName);
			return facade.getSegment(index);
		} else {
			return null;
		}
	}
	
	public List<MpegtsSegment> getSegmentList(String scopeName, String streamName) {
		ConcurrentHashMap<String, SegmentFacade> segments = scopeSegMap.get(scopeName);
		if (segments.containsKey(streamName)) {
			SegmentFacade facade = segments.get(streamName);
			return facade.segments;
		} else {
			return null;
		}
	}
	
	public boolean isAvailable(IScope scope, String streamName) {
		
		ConcurrentHashMap<String, SegmentFacade> segments = scopeSegMap.get(scope.getName());

		if (segments == null) {
			segments = new ConcurrentHashMap<String, MpegtsSegmenterService.SegmentFacade>();
			scopeSegMap.put(scope.getName(), segments);
		} 
		return segments.containsKey(streamName);
	}
	
	public void update(IBroadcastStream stream, IScope scope, String name, IRTMPEvent event) {
		ConcurrentHashMap<String, SegmentFacade> segments = scopeSegMap.get(scope.getName());
		if (segments == null) {
			segments = new ConcurrentHashMap<String, MpegtsSegmenterService.SegmentFacade>();
			scopeSegMap.put(scope.getName(), segments);
		}
		SegmentFacade facade = segments.get(name);
		if (facade == null) { //TODO http live stream aes 128是否加密在这里处理 
			facade = new SegmentFacade(name, Configuration.HLS_ENCRYPT);
			segments.put(name, facade);
		}
		try {
			facade.writeEvent(stream, event);
		} catch (IOException e) {
			log.info("write ts exception {}", e.getMessage());
		}
	}
	
	public String getSegmentEnckey(String scopeName, String streamName) {
		String encKey = null;
		ConcurrentHashMap<String, SegmentFacade> segments = scopeSegMap.get(scopeName);	
		if(segments != null) {
			SegmentFacade facade = segments.get(streamName);
			if(facade != null) encKey = facade.encKey;
		}
		return encKey;
	}
	
	public boolean getSegmentIsEncrypt(String scopeName, String streamName) {
		boolean isEncrypt = false;
		ConcurrentHashMap<String, SegmentFacade> segments = scopeSegMap.get(scopeName);	
		if(segments != null) {
			SegmentFacade facade = segments.get(streamName);
			if(facade != null) isEncrypt = facade.isEncrypt;
		}
		return isEncrypt;
	}
	
	public void removeSegment(String scopeName, String streamName) {
	
		ConcurrentHashMap<String, SegmentFacade> segments = scopeSegMap.get(scopeName);	
		if(segments != null) {
			SegmentFacade facade = segments.remove(streamName);
			if(facade != null) facade.close();
		}
	}
	
	@Override
	public void packetReceived(IBroadcastStream stream, IStreamPacket packet) {
		if(packet instanceof VideoData || packet instanceof AudioData) this.update(stream, stream.getScope(), stream.getPublishedName(), (IRTMPEvent)packet);
	}

	private class SegmentFacade {
		
		List<MpegtsSegment> segments = new ArrayList<MpegtsSegment>();		
		// segment currently being written to
		MpegtsSegment segment;		
		// segment index counter
		AtomicInteger counter = new AtomicInteger();
		// video and audio packet count
		AtomicInteger frameCounter = new AtomicInteger();
		boolean isEncrypt = false;
		String encKey;
		FLV2MPEGTSChunkWriter writer;
		
		String streamName;
		long startTimeStamp = -1L;
		long lastTimeStamp = 0;
		IoBuffer videoConfig;
		IoBuffer audioConfig;
		
		SegmentFacade(String streamName, boolean isEncrypt) {
			this.isEncrypt = isEncrypt;
			this.streamName = streamName;
			if (isEncrypt) {
				this.encKey = generatKey();
				log.info("http live stream publish, name : {}, is encrypt, enc key : {}", streamName, encKey);
			} else {
				log.info("http live stream publish : {}", streamName);
			}
		}
		
		public int getSegmentCount() {
			return segments.size();
		}
		
		public MpegtsSegment getSegment() {
			return segment;
		}
		
		public MpegtsSegment getSegment(int index) {

			for (MpegtsSegment segment : segments) {
				if (segment.getSequence() == index)
					return segment;
			}
			return null;
		}
		
		public void close() {
			writer = null;
			segments.clear();
			segment = null;
			videoConfig = null;
			audioConfig = null;
			log.info("http live stream unpublish, name : {}", streamName);
		}
		
		public void  writeEvent(IBroadcastStream stream, IRTMPEvent event) throws IOException{	
			// fix wait video and audio config 
			if(frameCounter.get() <= 20) {
				frameCounter.incrementAndGet();
				return;
			}
			
			if(startTimeStamp == -1L) startTimeStamp = event.getTimestamp(); 
			
			boolean newSegment = false;
			
			if (event.getTimestamp() > lastTimeStamp) {
				lastTimeStamp = event.getTimestamp();
			}
			
			if (segment == null) {
				if(event instanceof VideoData && ((VideoData) event).getFrameType() == FrameType.KEYFRAME) {
					segment = new MpegtsSegment(streamName, counter.incrementAndGet());
					segment.setEncKey(encKey);
					// flag that we created a new segment
					newSegment = true;
				} else {
					// first segment waiting video keyframe
					return;
				}
			} else {
				long currentSegmentTs = event.getTimestamp() - startTimeStamp; 
				if ((currentSegmentTs >= segmentTimeLimit) && event instanceof VideoData && ((VideoData) event).getFrameType() == FrameType.KEYFRAME) {
					
					writer.endChunkTS();
					// close active segment
					segment.close();
					segments.add(segment);
					startTimeStamp = event.getTimestamp();
					// create a segment
					segment = new MpegtsSegment(streamName, counter.incrementAndGet());
					segment.setEncKey(encKey);
					newSegment = true;
				}
			}
			
			if (newSegment) {				
				if (segments.size() > maxSegmentsPerFacade) {
					// get current segments index minux max
					int rmNum = segments.size() - maxSegmentsPerFacade;
					MpegtsSegment seg = null;
					if(rmNum > 0) {
						for(int i = 0; i < rmNum; i++) {
							seg = segments.remove(0);
							seg.dispose();
						}
					}					
				}
				
				// 音视频config信息
				if (videoConfig == null || audioConfig == null) {
					IStreamCodecInfo codecInfo = stream.getCodecInfo();
					IVideoStreamCodec videoCodecInfo = null;
					if (codecInfo != null && codecInfo.hasVideo()) {
						videoCodecInfo = codecInfo.getVideoCodec();
					}

					if (videoCodecInfo != null && videoCodecInfo.getDecoderConfiguration() != null) {
						videoConfig = videoCodecInfo.getDecoderConfiguration().asReadOnlyBuffer();
					}

					IAudioStreamCodec audioCodecInfo = null;
					if (codecInfo != null && codecInfo.hasAudio()) {
						audioCodecInfo = codecInfo.getAudioCodec();
					}

					if (audioCodecInfo != null && audioCodecInfo.getDecoderConfiguration() != null) {
						audioConfig = audioCodecInfo.getDecoderConfiguration().asReadOnlyBuffer();
					}
				}
								
				if (writer == null) {
					writer = new FLV2MPEGTSChunkWriter(videoConfig, audioConfig, isEncrypt);
				}
				
				writer.startChunkTS(segment);
			}
			writer.writeStreamEvent(event);
		}
	}
	
	public static String generatKey() {

		KeyGenerator kgen;
		String encKey = null;
		try {
			kgen = KeyGenerator.getInstance("AES");
			kgen.init(128);
			SecretKey skey = kgen.generateKey();
			byte[] raw = skey.getEncoded();
			encKey = HexDump.encodeHexString(raw);
		} catch (NoSuchAlgorithmException e) {
			log.error("generat hls key fail : {}", e.toString());
		}
		
		return encKey;
	}
}
