package com.sms.io.ts;

import static com.sms.io.ts.TransportStreamUtils.SYNCBYTE;
import static com.sms.io.ts.TransportStreamUtils.TIME_SCALE;
import static com.sms.io.ts.TransportStreamUtils.TS_PACKETLEN;
import static com.sms.io.ts.TransportStreamUtils.audioCodecToStreamType;
import static com.sms.io.ts.TransportStreamUtils.fillPAT;
import static com.sms.io.ts.TransportStreamUtils.fillPMT;
import static com.sms.io.ts.TransportStreamUtils.videoCodecToStreamType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.flv.FLVUtils;
import com.sms.io.mp3.impl.MP3BufferedDecoder;
import com.sms.io.mp3.impl.MP3HeaderData;
import com.sms.io.utils.BufferUtils;
import com.sms.server.media.aac.AACFrame;
import com.sms.server.media.aac.AACUtils;
import com.sms.server.media.h264.H264CodecConfigParts;
import com.sms.server.media.h264.H264Utils;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.stream.codec.AudioCodec;
import com.sms.server.stream.codec.VideoCodec;

/**
 * FLV TO Mpeg2TS
 * @author pengliren
 *
 */
public class FLV2MPEGTSWriter {

	private static Logger log = LoggerFactory.getLogger(FLV2MPEGTSWriter.class);
	
	private IFLV2MPEGTSWriter writer;
	
	public final static int videoPID = 0x100;

	public final static int audioPID = 0x101;
	
	private byte videoStreamID = (byte) 0xE0;
	
	private byte audioStreamID = (byte) 0xC0;
	
	private byte[] block = new byte[TS_PACKETLEN];
	
	protected long videoCCounter = -1L;
	
	protected long audioCCounter = -1L;
	
	protected long patCCounter = 0L;
	
	protected AACFrame aacFrame = null;
	
	protected H264CodecConfigParts h264CodecConfigPart = null;
	
	protected long lastAudioTimecode = -1L;
	
	protected long lastVideoTimecode = -1L;
	
	protected long lastPCRTimecode = -1L;
	
	protected boolean isFirstAudioPacket = true;
	
	protected boolean isFirstVideoPacket = true;
	
	// fix rtmp mp3 to mpegts ts
	private int lastMP3SampleRate = -1;
	
	private long lastMP3Timecode = -1;
	
	private byte[] mp3HeaderBuf;
	
	private MP3HeaderData mp3HeaderData;
	
	// fix rtmp aac to mpegts ts
	private int lastAACSampleRate = -1;
	
	private long lastAACTimecode = -1L;
	
	protected WaitingAudio waitingAudio = new WaitingAudio();
	
	protected int pcrBufferTime = 750;	
	
	protected int mpegtsAudioGroupCount = 3;

	protected int videoCodec = TransportStreamUtils.STREAM_TYPE_VIDEO_UNKNOWN;
	
	protected int audioCodec = TransportStreamUtils.STREAM_TYPE_AUDIO_UNKNOWN;
	
	public FLV2MPEGTSWriter(IFLV2MPEGTSWriter writer, IoBuffer videoConfig, IoBuffer audioConfig) {
		
		this.writer = writer;
		if(videoConfig != null) {
			videoCodec = (byte)FLVUtils.getVideoCodec(videoConfig.get(0));
			if(videoCodec == VideoCodec.AVC.getId()) {
				videoConfig.position(0);
				h264CodecConfigPart = H264Utils.breakApartAVCC(videoConfig);
			}
		}
		
		if(audioConfig != null) {
			audioCodec = (byte)FLVUtils.getAudioCodec(audioConfig.get(0));
			if(audioCodec == AudioCodec.AAC.getId()) {
				audioConfig.position(2);
				aacFrame = AACUtils.decodeAACCodecConfig(audioConfig);
			}
		}
	}
	
	public void handleVideo(VideoData data) {
		
		IoBuffer dataBuff = data.getData().asReadOnlyBuffer();
		dataBuff.mark();
		long ts90 = data.getTimestamp() * TIME_SCALE;
		lastVideoTimecode = ts90;
		int dataLen = dataBuff.remaining();
		byte[] dataBytes = new byte[dataLen];
		dataBuff.get(dataBytes);
		dataBuff.reset();		
		if (dataBuff.remaining() >= 2) {
			int firstByte = dataBuff.get();
			int secondByte = dataBuff.get();
			int codec = FLVUtils.getVideoCodec(firstByte);
			int frameType = FLVUtils.getFrameType(firstByte);
			int paloadLen = 0;
			int naluNum = 0;
			videoCodec = codec;
			if ((codec == VideoCodec.AVC.getId()) && (secondByte != 1)) {
				if (secondByte == 0) {
					dataBuff.position(0);
					if (h264CodecConfigPart == null) {
						h264CodecConfigPart = H264Utils.breakApartAVCC(dataBuff);
						dataBuff.position(5);
					}
				}
			} else if (codec == VideoCodec.AVC.getId()) {
				int cts = BufferUtils.byteArrayToInt(dataBytes, 2, 3) * TIME_SCALE;
				long ts = ts90 + cts;
				int ptdDtsFlag = 1;
				int loop = 5;
				int naluLen;
				dataBuff.position(5);
				List<TSPacketFragment> tsPacketList = new ArrayList<TSPacketFragment>();
				byte[] h264Startcode = new byte[4]; // h264 start code 0x00 0x00
				h264Startcode[3] = 1; // 0x00 0x01
				int sps = 0;
				int pps = 0;
				int pd = 0;

				while (loop + 4 <= dataLen) {
					naluLen = BufferUtils.byteArrayToInt(dataBytes, loop, 4);
					loop += 4;

					if ((naluLen <= 0) || (loop + naluLen > dataLen))break;
					int naluType = dataBytes[loop] & 0x1F;
					if (naluType == 7) { // sps
						sps = 1;
					} else if (naluType == 8) { // pps
						pps = 1;
					} else if (naluType == 9) { // pd
						pd = 1;
					}

					tsPacketList.add(new TSPacketFragment(h264Startcode, 0, h264Startcode.length));
					tsPacketList.add(new TSPacketFragment(dataBytes, loop, naluLen));
					paloadLen += (naluLen + h264Startcode.length);
					naluNum++;
					loop += naluLen;

					if (loop >= dataLen) break;
				}

				int idx = 0;
				if (pd == 0) {
					byte[] annex = new byte[6];
					annex[3] = 0x01;
					annex[4] = 0x09;
					if (frameType == 1) annex[5] = 16;
					else if (frameType == 3) annex[5] = 80;
					else annex[5] = 48;
					tsPacketList.add(idx, new TSPacketFragment(annex, 0, annex.length));
					idx++;
					paloadLen += annex.length;
					naluNum++;
					naluLen = 1;
				} else {
					idx = 2;
				}

				// sps and pps
				if (frameType == 1 && (pps == 0 || pd == 0)) {
					if (frameType == 1 && sps == 0 && h264CodecConfigPart != null && h264CodecConfigPart.getSps() != null) {
						byte[] h264SpsStartcode = new byte[4];
						h264SpsStartcode[3] = 1;
						tsPacketList.add(idx, new TSPacketFragment(h264SpsStartcode, 0, h264SpsStartcode.length));
						idx++;
						tsPacketList.add(idx, new TSPacketFragment(h264CodecConfigPart.getSps(), 0, h264CodecConfigPart.getSps().length));
						idx++;
						paloadLen += (h264CodecConfigPart.getSps().length + h264SpsStartcode.length);
						naluNum++;
						naluLen = 1;
					}

					if (frameType == 1 && pps == 0 && h264CodecConfigPart != null && h264CodecConfigPart.getPpss() != null) {
						List<byte[]> ppss = h264CodecConfigPart .getPpss();
						for (byte[] b : ppss) {
							byte[] h264PpsStartcode = new byte[4]; 
							h264PpsStartcode[3] = 1;
							tsPacketList.add(idx, new TSPacketFragment(h264PpsStartcode, 0, h264PpsStartcode.length));
							idx++;
							tsPacketList.add(idx, new TSPacketFragment(b, 0, b.length));
							idx++;
							paloadLen += (b.length + h264PpsStartcode.length);
							naluNum++;
							naluLen = 1;
						}
					}
				}

				if (naluNum > 0) {
					TSPacketFragment tsPacket = (TSPacketFragment) tsPacketList.remove(0);
					int offset = ((TSPacketFragment) tsPacket).getOffset();
					int len = ((TSPacketFragment) tsPacket).getLen();
					byte[] buff = ((TSPacketFragment) tsPacket).getBuffer();
					long pcr = getPCRTimecode();
					int paloadReadedLen = 0;
					int stt = 1; // pay_load_unit_start_indicator
					int pesPayloadWritten = 0;
					while (true) {
						int unReadPayloadLen = paloadLen - paloadReadedLen;
						stt = 1;
						int atf = 1; // adaption_field_control
						int readPayloadLen = 0;
						if (unReadPayloadLen > 32725) unReadPayloadLen = 32725; //maxPesDataLen is 32725

						while (true) {
							int tsIdx = 0;
							// ts header 4 byte
							block[tsIdx] = SYNCBYTE; // ts header start sync_byte 
							tsIdx++;
							block[tsIdx] = (byte) ((stt != 0 ? 64 : 0) + (0x1F & videoPID >> 8));
							tsIdx++;
							block[tsIdx] = (byte) (videoPID & 0xFF);
							tsIdx++;
							if (videoCCounter == -1L)
								videoCCounter = 1L;
							else
								videoCCounter += 1L;
							block[tsIdx] = (byte) (int) (16L + (videoCCounter & 0xF)); // ts header end 
							
							tsIdx++;
							int pesHeaderLen = 0;
							if (stt != 0)
								pesHeaderLen = 9 + (ptdDtsFlag != 0 ? 10 : 5);
							int tsPaloadLen = TS_PACKETLEN - tsIdx - pesHeaderLen;
							if (tsPaloadLen > unReadPayloadLen - readPayloadLen)
								tsPaloadLen = unReadPayloadLen - readPayloadLen;
							int atfLen;
							int fillNullLen;
							long tempts;

							if (atf != 0) {
								int thirdByte = 3;
								block[thirdByte] = (byte) (block[thirdByte] | 0x20);
								atfLen = 8;
								tsPaloadLen = TS_PACKETLEN - tsIdx - pesHeaderLen - atfLen;
								if (tsPaloadLen > unReadPayloadLen - readPayloadLen)
									tsPaloadLen = unReadPayloadLen - readPayloadLen;
								fillNullLen = 0;
								if (tsIdx + tsPaloadLen + pesHeaderLen + atfLen < TS_PACKETLEN)
									fillNullLen = TS_PACKETLEN - (tsIdx + tsPaloadLen + pesHeaderLen + atfLen);
								block[tsIdx] = (byte) (atfLen - 1 + fillNullLen & 0xFF);
								tsIdx++;
								block[tsIdx] = (byte) ((isFirstVideoPacket ? 0x80 : 16) | (frameType == 1 ? 64 : 0));
								tsIdx++;
								tempts = pcr;
								tempts <<= 7;
								byte[] pcrData = BufferUtils.longToByteArray(tempts);
								block[tsIdx + 4] = (byte) ((pcrData[7] & 0x80) + 126);
								block[tsIdx + 3] = (byte) (pcrData[6] & 0xFF);
								block[tsIdx + 2] = (byte) (pcrData[5] & 0xFF);
								block[tsIdx + 1] = (byte) (pcrData[4] & 0xFF);
								block[tsIdx] = (byte) (pcrData[3] & 0xFF);
								tsIdx += 6;
								if (fillNullLen > 0) {
									System.arraycopy(TransportStreamUtils.FILL, 0, block, tsIdx, fillNullLen);
									tsIdx += fillNullLen;
								}
								tsPaloadLen = TS_PACKETLEN - tsIdx - pesHeaderLen;
								if (tsPaloadLen > unReadPayloadLen - readPayloadLen)
									tsPaloadLen = unReadPayloadLen - readPayloadLen;
								atf = 0;
							} else if (tsIdx + tsPaloadLen + pesHeaderLen < TS_PACKETLEN) {
								atfLen = TS_PACKETLEN - (tsIdx + tsPaloadLen + pesHeaderLen);
								int third = 3;
								block[third] = (byte) (block[third] | 0x20);
								if (atfLen > 1) {
									atfLen--;
									block[tsIdx] = (byte) (atfLen & 0xFF);
									tsIdx++;
									block[tsIdx] = 0;
									tsIdx++;
									atfLen--;
									if (atfLen > 0)
										System.arraycopy(TransportStreamUtils.FILL, 0, block, tsIdx, atfLen);
									tsIdx += atfLen;
								} else {
									block[tsIdx] = 0;
									tsIdx++;
								}
							}
							if (stt != 0) { // pay_load_unit_start_indicator = 1
								block[tsIdx] = 0;
								tsIdx++;
								block[tsIdx] = 0;
								tsIdx++;
								block[tsIdx] = 1;
								tsIdx++;
								block[tsIdx] = videoStreamID;
								tsIdx++;
								atfLen = ptdDtsFlag != 0 ? 10 : 5;
								fillNullLen = unReadPayloadLen + atfLen + 3;
								if (fillNullLen >= 65536)
									log.warn("toolong: {}", fillNullLen);
								BufferUtils.intToByteArray(fillNullLen, block, tsIdx, 2);
								tsIdx += 2;
								block[tsIdx] = (byte) 0x84;
								tsIdx++;
								block[tsIdx] = (byte) (ptdDtsFlag != 0 ? 0xC0 : 0x80);
								tsIdx++;
								block[tsIdx] = (byte) atfLen;
								tsIdx++;
								tempts = ts;
								block[tsIdx + 4] = (byte) (int) (((tempts & 0x7F) << 1) + 1L);
								tempts >>= 7;
								block[tsIdx + 3] = (byte) (int) (tempts & 0xFF);
								tempts >>= 8;
								block[tsIdx + 2] = (byte) (int) (((tempts & 0x7F) << 1) + 1L);
								tempts >>= 7;
								block[tsIdx + 1] = (byte) (int) (tempts & 0xFF);
								tempts >>= 8;
								block[tsIdx] = (byte) (int) (((tempts & 0x7) << 1) + 1L + (ptdDtsFlag != 0 ? 48 : 32));
								tsIdx += 5;
								if (ptdDtsFlag != 0) {
									tempts = ts90;
									block[tsIdx + 4] = (byte) (int) (((tempts & 0x7F) << 1) + 1L);
									tempts >>= 7;
									block[tsIdx + 3] = (byte) (int) (tempts & 0xFF);
									tempts >>= 8;
									block[tsIdx + 2] = (byte) (int) (((tempts & 0x7F) << 1) + 1L);
									tempts >>= 7;
									block[tsIdx + 1] = (byte) (int) (tempts & 0xFF);
									tempts >>= 8;
									block[tsIdx] = (byte) (int) (((tempts & 0x7) << 1) + 1L + (ptdDtsFlag != 0 ? 16 : 32));
									tsIdx += 5;
								}
							}

							while (true) {
								atfLen = tsPaloadLen;
								if (atfLen > len - pesPayloadWritten)
									atfLen = len - pesPayloadWritten;
								System.arraycopy(buff, offset + pesPayloadWritten, block, tsIdx, atfLen);
								pesPayloadWritten += atfLen;
								tsIdx += atfLen;
								paloadReadedLen += atfLen;
								tsPaloadLen -= atfLen;
								readPayloadLen += atfLen;
								if (pesPayloadWritten >= len) {
									pesPayloadWritten = 0;
									if (tsPacketList.size() > 0) {
										tsPacket = (TSPacketFragment) tsPacketList.remove(0);
										offset = ((TSPacketFragment) tsPacket).getOffset();
										len = ((TSPacketFragment) tsPacket).getLen();
										buff = ((TSPacketFragment) tsPacket).getBuffer();
									}
								}
								if (tsIdx >= TS_PACKETLEN || readPayloadLen >= unReadPayloadLen || paloadReadedLen >= paloadLen) break;
							}
							stt = 0;
							writer.nextBlock(ts90, block);
							isFirstVideoPacket = false;
							if (readPayloadLen >= unReadPayloadLen || paloadReadedLen >= paloadLen) break;
						}
						if (paloadReadedLen >= paloadLen) break;
					}
				}
			} else {
				log.debug("video data is not h264/avc!");
			}
		}
	}
	
	public void handleAudio(AudioData data) {
		
		IoBuffer dataBuff = data.getData().asReadOnlyBuffer();
		dataBuff.mark();
		int dataLen = dataBuff.remaining();
		byte[] dataBytes = new byte[dataLen];
		dataBuff.get(dataBytes);
		dataBuff.reset();
		byte firstByte = dataBuff.get();
		byte secondByte = dataBuff.get();
		int codecId = FLVUtils.getAudioCodec(firstByte);
		audioCodec = codecId;
		if (codecId == AudioCodec.AAC.getId() && secondByte != 1) {
			if (secondByte == 0) {
				AACFrame tempFrame = AACUtils.decodeAACCodecConfig(dataBuff);
				if (aacFrame == null && tempFrame != null) {
					aacFrame = tempFrame;
				}
				if(tempFrame == null) log.error("audio error configure:{}", dataBuff);
			}
		} else if ((codecId == AudioCodec.AAC.getId() || codecId == AudioCodec.MP3.getId()) 
				&& (codecId != AudioCodec.AAC.getId() || aacFrame != null)) {
			long ts = data.getTimestamp() * TIME_SCALE;
			long incTs;
			long fixTs;
			int interval = -1;
			// fix low-resolution timestamp in RTMP to MPEG-TS
			if(codecId == AudioCodec.AAC.getId()) {
				 
				if(lastAACSampleRate == -1 || lastAACSampleRate != aacFrame.getSampleRate()) {
					lastAACSampleRate = this.aacFrame.getSampleRate();
	                lastAACTimecode = Math.round(data.getTimestamp() * lastAACSampleRate / 1000.0D);
				} else {
					incTs = lastAACTimecode + aacFrame.getSampleCount();
	                fixTs = Math.round(incTs * 1000.0D / lastAACSampleRate);
	                interval = (int)Math.abs(fixTs - data.getTimestamp());
					if (interval <= 1) {
						ts = Math.round(incTs * 90000L / lastAACSampleRate);
						lastAACTimecode = incTs;
					} else {
						lastAACTimecode = Math.round(data.getTimestamp() * lastAACSampleRate / 1000.0D);
					}
				}
				
				// aacFram size = 7 byte(adts header) + aac data size - 2 byte(0xAF 0x00)
				aacFrame.setSize(7 + dataBytes.length - 2);
				byte[] adts = new byte[7];
				AACUtils.frameToADTSBuffer(aacFrame, adts, 0);
				
				waitingAudio.fragments.add(new TSPacketFragment(adts, 0, adts.length));
				waitingAudio.size += adts.length;
				waitingAudio.fragments.add(new TSPacketFragment(dataBytes, 2, dataBytes.length - 2));
				waitingAudio.size += dataBytes.length - 2;
				waitingAudio.codec = codecId;				
			} else if(codecId == AudioCodec.MP3.getId()) {
				
				try {
					if (mp3HeaderBuf == null) {
						mp3HeaderBuf = new byte[4];
						mp3HeaderData = new MP3HeaderData();
					}
					System.arraycopy(dataBytes, 1, mp3HeaderBuf, 0, 4);
					int syncData = MP3BufferedDecoder.syncHeader((byte)0, mp3HeaderBuf, mp3HeaderData);
					if(syncData != 0) {
						MP3BufferedDecoder.decodeHeader(syncData, 0, mp3HeaderData);
						int sampleCount = MP3BufferedDecoder.samples_per_frame(this.mp3HeaderData);
		                int sampleRate = MP3BufferedDecoder.frequency(mp3HeaderData);
		                
		                if (lastMP3SampleRate == -1 || lastMP3SampleRate != sampleRate) {
							lastMP3SampleRate = sampleRate;
							lastMP3Timecode = Math.round(data.getTimestamp() * lastMP3SampleRate / 1000.0D);
						} else {
							incTs = lastMP3Timecode + sampleCount;
							fixTs = Math.round(incTs * 1000.0D / lastMP3SampleRate);
							interval = (int) Math.abs(fixTs - data.getTimestamp());
							if (interval <= 1) {
								ts = Math.round(incTs * 90000L / lastMP3SampleRate);
								lastMP3Timecode = incTs;
							} else {
								lastMP3Timecode = Math.round(data.getTimestamp() * lastMP3SampleRate / 1000.0D);
							}
						}
					}					
				} catch (Exception e) {
					log.error("mp3 header parse fail: {}", e.toString());
				}
				
				waitingAudio.fragments.add(new TSPacketFragment(dataBytes, 1, dataBytes.length - 1));
	            waitingAudio.size += dataBytes.length - 1;
	            waitingAudio.codec = codecId;
			}
			
			waitingAudio.count += 1;
			if (waitingAudio.timecode == -1L)
				waitingAudio.timecode = ts;
			waitingAudio.lastTimecode = ts;
			if (waitingAudio.count >= mpegtsAudioGroupCount) {
				lastAudioTimecode = waitingAudio.timecode;
				writeAudioPackets(waitingAudio);
				waitingAudio.clear();
			}
		}
	}
	
	/**
	 *  write mult audio packets
	 * @param waitingAudio
	 * @throws IOException 
	 */
	private void writeAudioPackets(WaitingAudio waitingAudio) {
		
		int size = waitingAudio.size;
		long ts = waitingAudio.timecode;
		long ts90 = waitingAudio.timecode;
		int ptdDtsFlag = 0;
		int totalWritten = 0;
		TSPacketFragment packetFragment = (TSPacketFragment) waitingAudio.fragments.remove(0);
		int writtenLen = 0;
		int offset = packetFragment.getOffset();
		int len = packetFragment.getLen();
		byte[] data = packetFragment.getBuffer();
		int stt = 1; // Payload unit start indicator.
		while (true) {
			
			int pos = 0;
			block[pos] = SYNCBYTE;
			pos++;
			block[pos] = (byte) ((stt != 0 ? 64 : 0) + (0x1F & audioPID >> 8));
			pos++;
			block[pos] = (byte) (audioPID & 0xFF);
			pos++;
			if (audioCCounter == -1L)
				audioCCounter = 1L;
			else
				audioCCounter += 1L;
			block[pos] = (byte) (int) (16L + (audioCCounter & 0xF));
			pos++;
			int pesHeaderLen = 0;
			if (stt != 0) pesHeaderLen = 9 + (ptdDtsFlag != 0 ? 10 : 5); // pes header len
			int count = TS_PACKETLEN - pos - pesHeaderLen;
			if (count > size - totalWritten) count = size - totalWritten;
			int total = 0;
			int pesLen;
			if (pos + count + pesHeaderLen < TS_PACKETLEN) { // 当数据不够188个字节时 需要用自适应区填满
				total = TS_PACKETLEN - (pos + count + pesHeaderLen);
				int thirdByte = 3;
				block[thirdByte] = (byte) (block[thirdByte] | 0x20);
				if (total > 1) {
					total--;
					block[pos] = (byte) (total & 0xFF);
					pos++;
					block[pos] = 0;
					pos++;
					total--;
					if (total > 0) System.arraycopy(TransportStreamUtils.FILL, 0, block, pos, total);
					pos += total;
				} else {
					block[pos] = 0;
					pos++;
				}
			}
			if (stt != 0) {
				// packet_start_code_prefix 0x000001
				block[pos] = 0x00;
				pos++;
				block[pos] = 0x00;
				pos++;
				block[pos] = 0x01;
				pos++;
				block[pos] = audioStreamID; // stream_id
				pos++;
				total = ptdDtsFlag != 0 ? 10 : 5;
				pesLen = size + total + 3;
				BufferUtils.intToByteArray(pesLen, block, pos, 2); // PES_packet_length
				pos += 2;
				block[pos] = (byte)0x80;
				pos++;
				block[pos] = (byte) (ptdDtsFlag != 0 ? 0xC0 : 0x80);
				pos++;
				block[pos] = (byte) total;
				pos++;
				block[pos + 4] = (byte) (int) (((ts & 0x7F) << 1) + 1L);
				ts >>= 7;
				block[pos + 3] = (byte) (int) (ts & 0xFF);
				ts >>= 8;
				block[pos + 2] = (byte) (int) (((ts & 0x7F) << 1) + 1L);
				ts >>= 7;
				block[pos + 1] = (byte) (int) (ts & 0xFF);
				ts >>= 8;
				block[pos] = (byte) (int) (((ts & 0x7) << 1) + 1L + (ptdDtsFlag != 0 ? 48 : 32));
				pos += 5;
				if (ptdDtsFlag != 0) {
					block[pos + 4] = (byte) (int) (((ts & 0x7F) << 1) + 1L);
					ts >>= 7;
					block[pos + 3] = (byte) (int) (ts & 0xFF);
					ts >>= 8;
					block[pos + 2] = (byte) (int) (((ts & 0x7F) << 1) + 1L);
					ts >>= 7;
					block[pos + 1] = (byte) (int) (ts & 0xFF);
					ts >>= 8;
					block[pos] = (byte) (int) (((ts & 0x7) << 1) + 1L + 32L);
					pos += 5;
				}
			}
			while (true) {
				total = count;
				if (total > len - writtenLen)
					total = len - writtenLen;
				System.arraycopy(data, offset + writtenLen, block, pos, total);
				writtenLen += total;
				pos += total;
				totalWritten += total;
				count -= total;
				if (writtenLen >= len) {
					writtenLen = 0;
					if (waitingAudio.fragments.size() > 0) {
						packetFragment = (TSPacketFragment) waitingAudio.fragments.remove(0);
						offset = packetFragment.getOffset();
						len = packetFragment.getLen();
						data = packetFragment.getBuffer();
					}
				}
				if (pos >= TS_PACKETLEN || totalWritten >= size) break;
			}
			stt = 0;
			writer.nextBlock(ts90, block);
			if (totalWritten >= size) break;
		}
	}
	
	/**
	 * add pat pmt
	 * @return
	 */
	public void addPAT(long ts) {		
		fillPAT(block, 0, patCCounter);
		writer.nextBlock(ts, block);
		fillPMT(block, 0, patCCounter, videoPID, audioPID, videoCodecToStreamType(videoCodec), audioCodecToStreamType(audioCodec));
		writer.nextBlock(ts, block);
		this.patCCounter++;
	}
	
	private long getPCRTimecode() {
		long ts = -1L;
		if ((lastAudioTimecode >= 0L) && (lastVideoTimecode >= 0L))
			ts = Math.min(lastAudioTimecode, lastVideoTimecode);
		else if (lastAudioTimecode >= 0L)
			ts = lastAudioTimecode;
		else if (lastVideoTimecode >= 0L)
			ts = lastVideoTimecode;
		if ((lastPCRTimecode != -1L) && (ts < lastPCRTimecode))
			ts = lastPCRTimecode;
		if (ts < 0L)
			ts = 0L;
		if (ts >= pcrBufferTime)
		      ts -= pcrBufferTime;
		lastPCRTimecode = ts;
		return ts;
	}
	
	public long getLastPCRTimecode() {
		return lastPCRTimecode;
	}

	public void setLastPCRTimecode(long lastPCRTimecode) {
		this.lastPCRTimecode = lastPCRTimecode;
	}

	public long getVideoCCounter() {
		return videoCCounter;
	}

	public int getVideoCodec() {
		return videoCodec;
	}

	public int getAudioCodec() {
		return audioCodec;
	}



	/**
	 * 
	 * @author pengliren
	 *
	 */
	class WaitingAudio {

		long timecode = -1L;
		long lastTimecode = -1L;
		int count = 0;
		int size = 0;
		int codec = 0;
		List<TSPacketFragment> fragments = new ArrayList<TSPacketFragment>();

		WaitingAudio() {
		}

		public void clear() {
			timecode = -1L;
			lastTimecode = -1L;
			count = 0;
			size = 0;
			fragments.clear();
		}

		public boolean isEmpty() {
			return size == 0;
		}

		public int size() {
			return size;
		}
	}
}
