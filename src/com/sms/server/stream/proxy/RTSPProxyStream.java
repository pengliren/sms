package com.sms.server.stream.proxy;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.event.IEvent;
import com.sms.server.api.service.IPendingServiceCall;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.rtp.RTPPacket;

/**
 * rtsp proxy stream
 * @author pengliren
 * 
 */
public abstract class RTSPProxyStream extends BaseRTMPProxyStream {

	private static Logger log = LoggerFactory.getLogger(RTSPProxyStream.class);

	private volatile IoBuffer currentNalu;

	private IoBuffer avcConfig;

	private IoBuffer aacConfig;
	
	private int videoTimescale;
	
	private int audioTimescale;
	
	private volatile boolean hasVideo = false;
	
	private volatile boolean hasAudio = false;

	public RTSPProxyStream(String streamName) {

		super();
		setPublishedName(streamName);
	}

	@Override
	public void resultReceived(IPendingServiceCall call) {

		log.info("rtsp proxy handle call result:{}", call);
	}

	public void handleMessage(RTPPacket packet) throws Exception {
		
		byte channel = packet.getChannel();
		switch (channel) {
		case 0x00:// video
			if(hasVideo) encodeVideoData(packet, videoTimescale);
			break;
		case 0x02:// audio
			if(hasAudio) encodeAudioData(packet, audioTimescale);
			break;
		default:// unkown
			break;
		}
	}

	private void encodeVideoData(RTPPacket packet, int timeScale) throws Exception {
		IoBuffer data = IoBuffer.wrap(packet.getPayload());
		byte naluHeader = data.get();
		int naluType = naluHeader & 0x1F;
		if ((naluType >=0) && (naluType <= 23)) { // Single NALU Packet 
			data.position(0);
			IoBuffer videoData = IoBuffer.allocate(2 + 3 + 4 + data.remaining()).setAutoExpand(true);
			if (naluType == 5) {
				videoData.put((byte) 0x17); // keyframe
			}
			else videoData.put((byte) 0x27);// nonkeyframe
			videoData.put((byte) 0x01);
			videoData.put((byte) 0);
			videoData.put((byte) 0);
			videoData.put((byte) 0);
			videoData.putInt(data.remaining());
			videoData.put(data);
			videoData.flip();
			VideoData vData = new VideoData();
			vData.setData(videoData);
			vData.setTimestamp(Math.round((float) packet.getTimestamp() / (timeScale / 1000)));
			dispatchEvent(vData);
		} else if (naluType == 28) {// NALU_TYPE_FUA
			byte fuHeader = data.get();
			int fuNaluType = fuHeader & 0x1F;
			//System.err.println("nalue type : "+fuNaluType);
			int fuHeaderS = (fuHeader & 0xFF) >> 7;
			// first NAL
			if (fuHeaderS == 1) {
				//System.err.println("start");
				currentNalu = IoBuffer.allocate(1500).setAutoExpand(true);
				byte nalu = (byte) (((data.get(0) & 0xE0) | (data.get(1) & 0x1F)) & 0xFF);
				currentNalu.put(nalu); // put 1byte nalu header
				currentNalu.put(data); // put nalu data
				return;
			} else {
				// middle NAL
				if(currentNalu == null) return;
				currentNalu.put(data);
				
				if ((((fuHeader & 0xFF) >> 6) & 0x01) == 1) { // FU Finish
					currentNalu.flip();
					IoBuffer videoData = IoBuffer.allocate(2 + 3 + 4 + currentNalu.remaining()).setAutoExpand(true);
					if (fuNaluType == 5) {
						videoData.put((byte) 0x17); // keyframe
					}
					else videoData.put((byte) 0x27); // nonkeyframe
					videoData.put((byte) 0x01);
					videoData.put((byte) 0);
					videoData.put((byte) 0);
					videoData.put((byte) 0);
					videoData.putInt(currentNalu.remaining());
					videoData.put(currentNalu);
					videoData.flip();
					VideoData vData = new VideoData();
					vData.setData(videoData);
					vData.setTimestamp(Math.round((float) packet.getTimestamp() / (timeScale / 1000)));
					dispatchEvent(vData);
					currentNalu = null;
				}
			}
		} else if (naluType == 24) {// NALU_TYPE_STAPA			
			log.info("rtsp proxy stream unsupported packet type: STAP-A");
		} else if(naluType == 25) { // STAP-B 
			log.info("rtsp proxy stream unsupported packet type: STAP-B");
		} else if(naluType == 26) {// MTAP-16
			log.info("rtsp proxy stream unsupported packet type: MTAP-16");
		} else if(naluType == 27) {// MTAP-24
			log.info("rtsp proxy stream unsupported packet type: MTAP-24");
		} else if(naluType == 29) {// FU-B
			log.info("rtsp proxy stream unsupported packet type: FU-B");
		} else {// unknow
			log.info("rtsp proxy stream unsupported packet type: unknow");
		}
	}

	private void encodeAudioData(RTPPacket packet, int timeScale) {

		IoBuffer data = IoBuffer.wrap(packet.getPayload());
		
		if(data.remaining() <= 4) return;
		
		// auheader start code 0x00, 0x10, ausize
		data.skip(2); // skip start code 2byte
		int au13 = ((data.get() << 5) & 0x1FE0) & 0xFFFF;
		int au3 = ((data.get() >> 3) & 0x1F) & 0xFF;
		int ausize = au13 | au3;
		log.debug("aac au size : "+ ausize +" packe len : "+data.remaining());
		
		byte[] aacPload = new byte[data.remaining()];
		data.get(aacPload);
		IoBuffer aacData = IoBuffer.allocate(2 + aacPload.length);
		aacData.put((byte)0xaf);
		aacData.put((byte)0x01);
		aacData.put(aacPload);
		aacData.flip();
		
		AudioData aData = new AudioData();
		aData.setData(aacData);
		aData.setTimestamp(Math.round((float) packet.getTimestamp() / (timeScale / 1000)));
		dispatchEvent(aData);
	}
	
	@Override
	public void dispatchEvent(IEvent event) {
		super.dispatchEvent(event);
	}

	public void setAVCConfig(IoBuffer avcConfig) {
		hasVideo = true;
		this.avcConfig = avcConfig;
		VideoData vData = new VideoData();
		vData.setData(avcConfig);
		vData.setTimestamp(0);
		dispatchEvent(vData);		
	}

	public void setAACConfig(IoBuffer aacConfig) {
		hasAudio = true;
		this.aacConfig = aacConfig;
		AudioData aData = new AudioData();
		aData.setData(aacConfig);
		aData.setTimestamp(0);
		dispatchEvent(aData);		
	}

	public int getVideoTimescale() {
		return videoTimescale;
	}

	public void setVideoTimescale(int videoTimescale) {
		this.videoTimescale = videoTimescale;
	}

	public int getAudioTimescale() {
		return audioTimescale;
	}

	public void setAudioTimescale(int audioTimescale) {
		this.audioTimescale = audioTimescale;
	}

	public IoBuffer getAVCConfig() {
		return avcConfig;
	}

	public IoBuffer getAACConfig() {
		return aacConfig;
	}
}
