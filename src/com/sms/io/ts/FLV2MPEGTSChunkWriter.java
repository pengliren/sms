package com.sms.io.ts;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.event.IEvent;
import com.sms.server.net.http.stream.MpegtsSegment;
import com.sms.server.net.http.stream.MpegtsSegmentEncryptor;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.VideoData;

/**
 * FLV TO Mpeg2TS Chunk Writer
 * @author pengliren
 *
 */
public class FLV2MPEGTSChunkWriter implements IFLV2MPEGTSWriter {

	private static Logger log = LoggerFactory.getLogger(FLV2MPEGTSChunkWriter.class);
	
	private IoBuffer data;
	
	private boolean init = false;
	
	private FLV2MPEGTSWriter flv2tsWriter;
	
	private boolean isEncrypt = false;
	
	private MpegtsSegmentEncryptor encryptor;
	
	public FLV2MPEGTSChunkWriter(IoBuffer videoConfig, IoBuffer audioConfig, boolean isEncrypt) {
	
		flv2tsWriter = new FLV2MPEGTSWriter(this, videoConfig, audioConfig);
		flv2tsWriter.setLastPCRTimecode(0);
		
		this.isEncrypt = isEncrypt;
		if(isEncrypt) {
			encryptor = new MpegtsSegmentEncryptor();
		}
	}
	
	private void initTsHeader() {

		if (init) return;
		flv2tsWriter.addPAT(0);	
		init = true;
	}
	
	/**
	 * next ts packet
	 * @throws IOException 
	 */
	@Override
	public void nextBlock(long ts , byte[] block) {
		byte[] encData;
		if (isEncrypt)
			encData = encryptor.encryptChunk(block, 0, block.length);
		else 
			encData = block;
		data.put(encData);
	}
	
	/**
	 * start write chunk ts
	 * @param os
	 */
	public void startChunkTS(MpegtsSegment segment) {
		
		this.data = segment.getBuffer();
		if (isEncrypt) {
			encryptor.init(segment.getEncKeyBytes(), segment.getSequence());
		}
		
		initTsHeader();
		log.debug("ts chunk start!");
	}
	
	public void startChunkTS(IoBuffer data) {
		
		this.data = data;		
		initTsHeader();
		log.debug("ts chunk start!");
	}
	
	/**
	 * end write chunk ts
	 */
	public void endChunkTS() {		
		if (isEncrypt) {
			byte[] encData = encryptor.encryptFinal();
			data.put(encData);
		}
		init = false;
		log.debug("ts chunk end!");
	}
	
	/**
	 * write stream 
	 * @param event
	 * @param ts
	 * @throws IOException
	 */
	public void writeStreamEvent(IEvent event) throws IOException {
		
		if(event == null) return;
		
		if (event instanceof VideoData) {
			flv2tsWriter.handleVideo((VideoData) event);
		} else if (event instanceof AudioData) {
			flv2tsWriter.handleAudio((AudioData) event);
		}
	}
}
