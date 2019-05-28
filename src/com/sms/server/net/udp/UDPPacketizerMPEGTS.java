package com.sms.server.net.udp;

import static com.sms.io.ts.TransportStreamUtils.TIME_SCALE;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.ts.FLV2MPEGTSWriter;
import com.sms.io.ts.IFLV2MPEGTSWriter;
import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.util.SystemTimer;
/**
 * UDP Mpegts
 * @author pengliren
 *
 */
public class UDPPacketizerMPEGTS implements IUDPPacketizer, IFLV2MPEGTSWriter {

	private static Logger log = LoggerFactory.getLogger(UDPPacketizerMPEGTS.class);
	
	private FLV2MPEGTSWriter flv2tsWriter;
	
	private IUDPTransportOutgoingConnection conn;
	
	protected long lastPAT = -1L;
	
	protected int mpegtsPacketsPerBlock = 7;
	
	private IoBuffer buffer;
	
	private AtomicInteger pcount = new AtomicInteger(0);
	
	private AtomicInteger frameCounter = new AtomicInteger();
	
	private IoBuffer videoConfig;
	
	private IoBuffer audioConfig;
	
	private boolean init = false;
	
	public UDPPacketizerMPEGTS(IUDPTransportOutgoingConnection conn) {
		
		this.conn = conn;		
	}
	
	@Override
	public void nextBlock(long ts, byte[] block) {
		if(buffer == null) {
			buffer = IoBuffer.allocate(1500).setAutoExpand(true);
		}
		
		if(pcount.get() >= mpegtsPacketsPerBlock) {
			buffer.flip();
			flushBlock();
			pcount.set(0);
			buffer.position(0);
		}
		buffer.put(block);
		pcount.incrementAndGet();
	}
	
	private void flushBlock() {

		byte[] payload = new byte[buffer.remaining()];
		buffer.get(payload);
		conn.sendMessage(payload, 0, payload.length);
	}

	@Override
	public void handleStreamPacket(IStreamPacket packet) {
		
		if (!init) {
			flv2tsWriter = new FLV2MPEGTSWriter(this, videoConfig, audioConfig);
			init = true;
			log.info("flv to mepgts udp stream!");
		}
		
		long cts = SystemTimer.currentTimeMillis();
		long ts = packet.getTimestamp() * TIME_SCALE;
		if ((lastPAT == -1L) || (cts - lastPAT > 100L)) {
			lastPAT = cts;
			flv2tsWriter.addPAT(ts);
		}
		
		if (packet instanceof VideoData) { // handle video
			flv2tsWriter.handleVideo((VideoData) packet);
		} else if (packet instanceof AudioData) {// handle audio
			flv2tsWriter.handleAudio((AudioData) packet);
		}
	}

	@Override
	public void stop() {
		
		if(conn != null) {
			conn.close();
		}
	}

	@Override
	public IUDPTransportOutgoingConnection getConnection() {
		return conn;
	}
	
	@Override
	public IoBuffer getAudioConfig() {

		return audioConfig;
	}
	
	@Override
	public void setAudioConfig(IoBuffer config) {

		audioConfig = config;
	}
	
	@Override
	public void setVideoConfig(IoBuffer config) {

		videoConfig = config;
	}
	
	@Override
	public IoBuffer getVideoConfig() {

		return videoConfig;
	}
	
	@Override
	public AtomicInteger getFrameCount() {

		return frameCounter;
	}
	
	@Override
	public boolean isInit() {
	
		return init;
	}
}
