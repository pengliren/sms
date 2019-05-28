package com.sms.io.ts;

import static com.sms.io.ts.TransportStreamUtils.STREAM_TYPE_AUDIO_AAC;
import static com.sms.io.ts.TransportStreamUtils.STREAM_TYPE_AUDIO_MPEG1;
import static com.sms.io.ts.TransportStreamUtils.STREAM_TYPE_AUDIO_MPEG2;
import static com.sms.io.ts.TransportStreamUtils.STREAM_TYPE_VIDEO_H264;
import static com.sms.io.ts.TransportStreamUtils.SYNCBYTE;
import static com.sms.io.ts.TransportStreamUtils.TIME_SCALE;
import static com.sms.io.ts.TransportStreamUtils.TS_PACKETLEN;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.event.IEventDispatcher;
import com.sms.server.media.aac.AACFrame;
import com.sms.server.media.aac.AACUtils;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.udp.IUDPMessageHandler;
import com.sms.server.net.udp.IUDPTransportSession;

/**
 * MPEGTS TO FLV Handler
 * MPEGTS Video: AVC
 * MPEGTS Audio: AAC MP3
 * @author pengliren
 *
 */
public class MPEGTS2FLVHandler implements IUDPMessageHandler {
	
	private static Logger log = LoggerFactory.getLogger(MPEGTS2FLVHandler.class);
	
	/** Last Not Reader TS Packet **/
	private IoBuffer lastTSPacket;
	
	/** Packet ID of the PAT (is always 0). **/
	private int patId = 0;
	
	/** Packet ID of the Program Map Table. **/
	private int pmtId = -1;
	
	/** Packet ID of the MP3 audio stream. **/
	private int mp3Id = -1;

	/** Packet ID of the AAC audio stream. **/
	private int aacId = -1;
	
	/** Packet ID of the video stream. **/
	private int avcId = -1;

	/** Current Video PES Packet **/
	private PES currentVideoPes;
	
	/** Current Audio PES Packet **/
	private PES currentAudioPes;
	
	/** AVC Config **/
	private IoBuffer avcConfig;
	
	private boolean avcConfigIsSender = false;
	
	/** AAC Config **/
	private IoBuffer aacConfig;
	
	private boolean aacConfigIsSender = false;
	
	private IEventDispatcher event;
	
	private byte[] singleTSData = new byte[TS_PACKETLEN];
	
	public MPEGTS2FLVHandler() {
		
	}

	@Override
	public void handleMessage(SocketAddress address, IoBuffer buffer) {
		
		// If last ts packet is not null, we must append
		if(lastTSPacket != null && lastTSPacket.hasRemaining()) {
			int len = lastTSPacket.remaining() + buffer.remaining();
			IoBuffer temp = IoBuffer.allocate(len);
			temp.put(lastTSPacket);
			temp.put(buffer);
			temp.flip();
			lastTSPacket = temp;
		}
		else {
			lastTSPacket = buffer;
		}
		
		while(lastTSPacket.remaining() >= TS_PACKETLEN) {
			
			int pos = checkTSPacketSyncPos(lastTSPacket);
			if(pos == -1) {
				lastTSPacket = null; 
				return;
			}
			lastTSPacket.position(pos);
			lastTSPacket.get(singleTSData);
			parsePacket(IoBuffer.wrap(singleTSData));
		}
	}
	
	/**
	 * check ts pakcet sync position
	 * @param buffer
	 * @return
	 */
	private int checkTSPacketSyncPos(IoBuffer buffer) {
		
		int pos = -1;
		while(buffer.get() == SYNCBYTE) {
			pos = buffer.position() - 1;
		}
		return pos;
	}

	@Override
	public void sessionOpened(IUDPTransportSession session) {
		
		log.info("session open");
	}

	@Override
	public void sessionClosed(IUDPTransportSession session) {
		
		log.info("session close");
	}
	
	/**
	 * Parse TS Packet
	 * @param data
	 */
	private void parsePacket(IoBuffer data) {
				
		int todo = TS_PACKETLEN;
		// Each packet is 188 bytes.
		if(data.remaining() < TS_PACKETLEN) return;
		
		todo--;
		
		// Sync byte.
		if(data.get() != SYNCBYTE) {
			throw new MPEG2TSParseException("Could not parse TS file: sync byte not found.");
		}
		
		// Payload unit start indicator.
		// 0x40(16) = 64(10) = 01000000(2)
		data.mark(); 
		int stt = ((data.get() & 0x40) >> 6) & 0xFF;
		data.reset();
		
		// Packet ID (last 13 bits of UI16).
		int pid = data.getShort() & 0x1FFF;
		
		todo -= 2;
		
		int atf = ((data.get() & 0x30) >> 4) & 0xFF;
		
		todo--;
		
		// Read adaptation field if available.
		if(atf > 1) {
			
			// Length of adaptation field.
			int len = data.getUnsigned();
			todo--;
			// Random access indicator (keyframe).
			data.skip(len);
			todo -= len;
			// Return if there's only adaptation field.
			if(atf == 2 || len == 183) {
				data.skip(todo);
				return;
			}
		}

		// Parse the PES, split by Packet ID.
		if(pid == patId) {
			parsePAT(data);
		} else if(pid == pmtId) {
			parsePMT(data);
		} else if(pid == mp3Id) {
			if(stt == 0x01) { // pes start
				if(currentAudioPes != null) parseMPEG(currentAudioPes); 
				currentAudioPes = new PES(true);
				currentAudioPes.append(data);
			} else if(currentAudioPes != null) {
				currentAudioPes.append(data);
			}
		} else if(pid == aacId) {
			if(stt == 0x01) { // pes start
				if(currentAudioPes != null) parseADTS(currentAudioPes); 
				currentAudioPes = new PES(true);
				currentAudioPes.append(data);
			} else if(currentAudioPes != null) {
				currentAudioPes.append(data);
			}
		} else if(pid == avcId) {
			if(stt == 0x01) { // pes start
				if(currentVideoPes != null) parseNALU(currentVideoPes);
				currentVideoPes = new PES(false);
				currentVideoPes.append(data);
			} else if(currentVideoPes != null) {
				currentVideoPes.append(data);
			}
		} else {
			// Ignored other packet IDs
			log.debug("unkown pid : " + pid);
		}
	}
	
	/**
	 * Parse the Program Association Table.
	 * @param data
	 */
	private void parsePAT(IoBuffer data) {
		// Check the section length for a single PMT.
		data.skip(3);
		
		if(data.getUnsigned() > 13) {
			throw new MPEG2TSParseException("Multiple PMT/NIT entries are not supported.");
		}
		
		// Grab the PMT ID.
		data.skip(7);
		pmtId = data.getShort() & 0x1FFF;
	}
	
	/**
	 * Parse the Program Map Table.
	 * @param data
	 */
	private void parsePMT(IoBuffer data) {	
		// Check the section length for a single PMT.
		data.skip(3);
		int len = data.getUnsigned();
		int read = 13;
		data.skip(8);
		
		int pil = data.getUnsigned();
		read += pil;
		data.skip(pil);
		// Loop through the streams in the PMT.
		while (read < len) {
			int typ = data.getUnsigned();
			int sid = data.getUnsignedShort() & 0x1FFF;
			if(typ == STREAM_TYPE_AUDIO_AAC) {
				aacId = sid;
			} else if (typ == STREAM_TYPE_VIDEO_H264) {
				avcId = sid;
			} else if (typ == STREAM_TYPE_AUDIO_MPEG1) {  
				mp3Id = sid;
			} else if(typ == STREAM_TYPE_AUDIO_MPEG2) {
				mp3Id = sid;
			}
			// Possible section length.
			data.skip(1);
			int sel = data.get() & 0x0F;
			data.skip(sel);
			read += (sel + 5);
		}
	}
	
	/**
	 * Get AAC Config Data.
	 * @param pes
	 * @return
	 */
	private IoBuffer getAACConfig(PES pes) {
				
		pes.data.mark();
        int sync = pes.data.getUnsignedShort();
        IoBuffer adif = null;
        if(sync == 0xFFF1 || sync == 0xFFF9) {
        	byte first = pes.data.get();
            int profile = (first & 0xF0) >> 6;
            // Correcting zero-index of ADIF and Flash playing only LC/HE.
            if (profile > 3) { 
            	profile = 5; 
            } else { 
            	profile = 2; 
            }
            
            int srate = (first & 0x3C) >> 2;
            pes.data.position(pes.data.position() - 1);
            int channels = (pes.data.getShort() & 0x01C0) >> 6;
            
            adif = IoBuffer.allocate(4);
            adif.put((byte)0xAF);
            adif.put((byte)0x00);
            // 5 bits profile + 4 bits samplerate + 4 bits channels.
            adif.put((byte)((byte)(profile << 3) + (byte)(srate >> 1)));
            adif.put((byte)((byte)(srate << 7) + (byte)(channels << 3)));
            adif.flip();
        } 
        
        pes.data.reset();
        if(adif == null) {
        	pes.data.skip(1);
            log.info("Stream did not start with ADTS header.");
        }
        
        return adif;
	}
	
	/**
	 * Parse MPEG data from audio PES streams.
	 * @param pes
	 */
	private void parseMPEG(PES pes) {
		
		pes.parsePES();
		AudioData audioData = new AudioData();
		audioData.setTimestamp(pes.pts);
		IoBuffer data = IoBuffer.allocate(1 + pes.data.remaining());
		data.put((byte)0x2F);
		data.put(pes.data);
		data.flip();
		audioData.setData(data);
		if(event != null) event.dispatchEvent(audioData);
	}
	
	/**
	 * Parse ADTS frames from audio PES streams. 
	 * @param pes
	 */
	private void parseADTS(PES pes) {

		pes.parsePES();
				
		// first get AAC Config
		while(aacConfig == null && pes.data.remaining() >= 7) {
			aacConfig = getAACConfig(pes);				
		}
		
		// if aacconfig is null, we must wait next data
		if(aacConfig == null) return;
		
		AudioData audioData;
		long ts;
		// send aac config
		if(!aacConfigIsSender) {
			audioData = new AudioData();
			audioData.setData(aacConfig);
			aacConfigIsSender = true;
			if(event != null) event.dispatchEvent(audioData);
		}
		
		AACFrame aacFrame;		
		List<AACFrame> aacFrames = new ArrayList<AACFrame>();
		while(pes.data.remaining() > 0) {
			aacFrame = AACUtils.decodeFrame(pes.data);
			if(aacFrame != null) aacFrames.add(aacFrame);
		}
		
		IoBuffer data;
		// if have multi audio packet, we must calculate timestamp agin
		for(int i = 0; i < aacFrames.size(); i++) {
			audioData = new AudioData();
			aacFrame = aacFrames.get(i);
			ts = Math.round(pes.pts + i * 1024 * 1000 / AACUtils.AAC_SAMPLERATES[aacFrame.getRateIndex()]);
			audioData.setTimestamp(ts);
			data = IoBuffer.allocate(2 + aacFrame.getDataLen());
			data.put((byte)0xAF);
			data.put((byte)0x01);
			data.put(aacFrame.getData());
			data.flip();
			audioData.setData(data);
			if(event != null) event.dispatchEvent(audioData);
		}
	}
	
	/**
	 * Parse NALU frames from video PES streams.
	 * @param pes
	 */
	private void parseNALU(PES pes) {

		pes.parsePES();
		List<NALU> nalus = getNALU(pes.data);
		
		// first we must get AVC Config
		if(avcConfig == null) {
			avcConfig = getAVCConfig(nalus);
		}
		
		// if avcconfig is null，we must wait next data
		if(avcConfig == null) return;
		
		VideoData videoData;
		
		// send avc config
		if(!avcConfigIsSender) {
			videoData = new VideoData();
			videoData.setData(avcConfig);
			avcConfigIsSender = true;
			if(event != null) event.dispatchEvent(videoData);
		}
		
		if(nalus.size() <= 0) return;
		
		IoBuffer data;
		long composition = 0;
		
		for(NALU nalu : nalus) {
			if(nalu.type > 5) continue; // Only push NAL units 1 to 5
			videoData = new VideoData();
			videoData.setTimestamp(pes.pts);
			data = IoBuffer.allocate(8192).setAutoExpand(true);
			if(nalu.isKeyframe) data.put((byte)0x17);
			else data.put((byte)0x27);
			data.put((byte)0x01);
			composition = pes.pts - pes.dts;  			
			data.put((byte) ((composition >>> 16) & 0xFF));
			data.put((byte) ((composition >>> 8) & 0xFF));
			data.put((byte) (composition & 0xFF));
			data.putInt(nalu.data.remaining());
			data.put(nalu.data);
			data.flip();
			videoData.setData(data);
			if(event != null) event.dispatchEvent(videoData);
		}
	}
	
	/**
	 * Get AVC Config Data
	 * @param pes
	 * @return
	 */
	private IoBuffer getAVCConfig(List<NALU> nalus) {
		// Find SPS and PPS units in AVC stream.
		int spsIdx = -1;
		int ppsIdx = -1;
        for(int i = 0; i < nalus.size(); i++) {
            if(nalus.get(i).type == 7 && spsIdx == -1) {
            	spsIdx = i;
            } else if (nalus.get(i).type == 8 && ppsIdx == -1) {
            	ppsIdx = i;
            }
        }
        // Throw errors if units not found.
        if(spsIdx == -1) {
            return null;
        } else if (ppsIdx == -1) {
            return null;
        }
        // Write startbyte, profile, compatibility and level.
        IoBuffer avcc = IoBuffer.allocate(128).setAutoExpand(true);
        avcc.put((byte)0x17);
        avcc.put((byte)0x00);
        avcc.put((byte)0x00);
        avcc.put((byte)0x00);
        avcc.put((byte)0x00);
        avcc.put((byte)0x01);
        IoBuffer spsBuffer = nalus.get(spsIdx).data; 
        byte[] sps = new byte[spsBuffer.remaining()];
        spsBuffer.get(sps);
        avcc.put(sps, 1, 3);
        // 111111 + NALU bytesize length (4?)
        avcc.put((byte)0xFF);
        // Number of SPS, Bytesize and data.
        avcc.put((byte)0xE1);
        avcc.putShort((short)sps.length);
        avcc.put(sps);
        // Number of PPS, Bytesize and data.
        IoBuffer ppsBuffer = nalus.get(ppsIdx).data; 
        byte[] pps = new byte[ppsBuffer.remaining()];
        ppsBuffer.get(pps);
        avcc.put((byte)0x01);
        avcc.putShort((byte)pps.length);
        avcc.put(pps);
        avcc.flip();
        return avcc;
	}
	
	/**
     * Return an array with NAL delimiter indexes.
     * @return
     */
    private static List<NALU> getNALU(IoBuffer data) {
    	
    	List<NALU> nalues = new ArrayList<NALU>();
    	NALU nalue;
    	// Loop through data to find NAL startcodes.
    	int window = 0;    	
    	
    	int startPos = -1;
    	int type = -1;
    	int header = 4;
    	
    	IoBuffer naluData;
    	while(data.remaining() > 4) {
    		window = (int)data.getUnsignedInt();
    		if((window & 0xFFFFFFFF) == 0x01) {
    			
    			if(startPos > 0) {
    				naluData = IoBuffer.wrap(data.array(), startPos, (data.position() - 4 - startPos));
    				nalue = new NALU(header, type, naluData);
    				nalues.add(nalue);
                }
    			header = 4;
    			startPos = data.position();
    			type = data.get() & 0x1F;
                if(type == 1 || type == 5) { break; }
    		} else if((window & 0xFFFFFF00) == 0x100) {
    			if(startPos > 0) {
    				naluData = IoBuffer.wrap(data.array(), startPos, (data.position() - 4 - startPos));
    				nalue = new NALU(header, type, naluData);
    				nalues.add(nalue);
                }
    			header = 3;
    			startPos = data.position();
    			type = data.get() & 0x1F;
                if(type == 1 || type == 5) { break; }
    		} else {
    			data.position(data.position() - 3);
    		}
    	}
        
        if(startPos > 0) {
			naluData = IoBuffer.wrap(data.array(), startPos, (data.limit() - startPos));
			nalue = new NALU(header, type, naluData);
			nalues.add(nalue);
        }
    	
    	return nalues;
    }
    
    /**
	 * Get a list with AAC frames from ADTS stream
	 * @param adts
	 * @return
	 */
	public static List<AACUtils> getFrames(IoBuffer adts) {
		
        return null;
	}

	public IEventDispatcher getEvent() {
		return event;
	}

	public void setEvent(IEventDispatcher event) {
		this.event = event;
	}
}

/**
 * TS PES
 * @author pengliren
 *
 */
class PES {
	/** Is it AAC audio or AVC video. **/
	boolean audio;

	/** The PES data (including headers). **/
	IoBuffer data;

	/** Start of the payload. **/
	int payload;

	/** Timestamp from the PTS header. **/
	long pts;
	
	/** Timestamp from the DTS header. **/
	long dts;
	
	PES(boolean isAudio) {
		
		this.data = IoBuffer.allocate(184).setAutoExpand(true);
		this.audio = isAudio;
	}
	
	/**
	 * Save the first chunk of PES data.
	 * @param data
	 * @param isVideo
	 */
	PES(IoBuffer data, boolean isAudio) {
		
		this.data = data;
		this.audio = isAudio;
	}
	
	void append(IoBuffer data) {
		
		this.data.put(data);
	}
	
	void parsePES() {
		
		data.flip();
        // Start code prefix and packet ID.
        long prefix = data.getUnsignedInt();
        if((audio && (prefix > 448 || prefix < 445)) ||
            (!audio && prefix != 480)) {
            throw new MPEG2TSParseException("PES start code not found or not AAC/AVC: " + prefix);
        }
        // Ignore packet length and marker bits.
        data.skip(3);
        // Check for PTS
        int flags = ((data.get() & 192) >> 6) & 0xFF;
        if(flags != 2 && flags != 3) {
            throw new MPEG2TSParseException("No PTS/DTS in this PES packet");
        }
        // Check PES header length
        int length = data.getUnsigned();
        // Grab the timestamp from PTS data (spread out over 5 bytes):
        // XXXX---X -------- -------X -------- -------X
        long pts = (((data.getUnsigned() & 14) << 29) +
            ((data.getUnsignedShort() & 65535) << 14) +
            ((data.getUnsignedShort() & 65535) >> 1)) & 0XFFFFFFFFL;
        length -= 5;
        long dts = pts;
        if(flags == 3) {
            // Grab the DTS (like PTS)
            dts = (((data.getUnsigned() & 14) << 29) +
                ((data.getUnsignedShort() & 65535) << 14) +
                ((data.getUnsignedShort() & 65535) >> 1)) & 0XFFFFFFFFL;
            length -= 5;
        }
        this.pts = Math.round(pts / TIME_SCALE);
        this.dts = Math.round(dts / TIME_SCALE);
        // Skip other header data and parse payload.
        data.skip(length);
        payload = data.position();
	}
}

/**
 * AVC NALU
 * @author pengliren
 *
 */
class NALU {
	/** NALU header size(4 bit or 3bit) **/
	int headerLen;
	
	/** NALU type **/
	int type;
	
	boolean isKeyframe = false;
	
	/** NALU paload **/
	IoBuffer data;
	
	NALU(int headerLen, int type, IoBuffer data) {
		this.headerLen = headerLen;
		this.type = type;
		this.data = data;
		if(type == 5) isKeyframe = true;
	}
}
