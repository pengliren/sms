package com.sms.io.ts;

import static com.sms.io.ts.TransportStreamUtils.STREAM_TYPE_AUDIO_AAC;
import static com.sms.io.ts.TransportStreamUtils.STREAM_TYPE_VIDEO_H264;
import static com.sms.io.ts.TransportStreamUtils.fillPAT;
import static com.sms.io.ts.TransportStreamUtils.fillPMT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.io.ITag;
import com.sms.io.flv.impl.FLVReader;
import com.sms.io.utils.BufferUtils;
import com.sms.server.media.aac.AACFrame;
import com.sms.server.media.aac.AACUtils;
import com.sms.server.media.h264.H264CodecConfigInfo;
import com.sms.server.media.h264.H264CodecConfigParts;
import com.sms.server.media.h264.H264Utils;
import com.sms.server.stream.codec.AudioCodec;

/**
 * flv to ts
 * @author pengliren
 *
 */
public class FLV2TSTest {

	private int videoPID = 0x100;
	private int audioPID = 0x101;
	private byte videoStreamID = (byte)0xE0;
	private byte audioStreamID = (byte)0xC0;
	
	public final static int TIME_SCALE = 90;
	
	private long lastAudioTimecode = -1L;
	private long lastVideoTimecode = -1L;
	private long lastPCRTimecode = -1L;
	
	private H264CodecConfigParts h264CodecConfigPart = null;
	private AACFrame aacFrame = null;
	
	private OutputStream os;
	
	private byte[] block = null;
	
	private long videoCCounter = -1L;
	private long audioCCounter = -1L;
	
	private boolean isFirstVideoPacket = true;
	
	@SuppressWarnings("unused")
	private boolean isFirstAudioPacket = true;
	
	private WaitingAudio waitingAudio = new WaitingAudio();
	
	private int audioGroupCount = 3;
	
	public FLV2TSTest(OutputStream os) throws IOException {
		this.os = os;
		byte[] pat = new byte[188];
		byte[] pmt = new byte[188];
		fillPAT(pat, 0, 0);
		fillPMT(pmt, 0, 0, videoPID, audioPID, STREAM_TYPE_VIDEO_H264, STREAM_TYPE_AUDIO_AAC);
		os.write(pat);
		os.write(pmt);
	}
	
	private void nextBlock() {
		this.block = new byte[188];
	}
	
	private void handleAudio(ITag data) throws IOException {
		
		IoBuffer dataBuff = data.getBody();
		byte[] dataBytes = dataBuff.array();
		byte firstByte = dataBuff.get();
		byte secondByte = dataBuff.get();
		if (((firstByte & 0xF0) >> 4) != AudioCodec.AAC.getId()) {
			return;
		}
		
		if (secondByte == 0) {
			AACFrame tempFrame = AACUtils.decodeAACCodecConfig(dataBuff);			
			if(aacFrame == null && tempFrame != null) { 				
				aacFrame = tempFrame;
				System.out.println(aacFrame);
			}
			return;
		}
		
      
	    long ts = data.getTimestamp() * TIME_SCALE;
	    aacFrame.setSize(7 + dataBytes.length - 2);
	    byte[] localObject1 = new byte[7];
	    AACUtils.frameToADTSBuffer(aacFrame, localObject1, 0);
	  
	    this.waitingAudio.fragments.add(new TSPacketFragment(localObject1, 0, localObject1.length));
        this.waitingAudio.size += localObject1.length;
        this.waitingAudio.fragments.add(new TSPacketFragment(dataBytes, 2, dataBytes.length - 2));
        this.waitingAudio.size += dataBytes.length - 2;
        this.waitingAudio.codec = 10;
		
        this.waitingAudio.count += 1;
        if (this.waitingAudio.timecode == -1L)
          this.waitingAudio.timecode = ts;
        this.waitingAudio.lastTimecode = ts;
        if (this.waitingAudio.count >= audioGroupCount)
        {
          this.lastAudioTimecode = this.waitingAudio.timecode;    
          writeAudioPackets(waitingAudio);
          this.waitingAudio.clear();
        }
	}
	
	private void writeAudioPackets(WaitingAudio paramWaitingAudio) throws IOException {
	
		int i = paramWaitingAudio.size;
	    long l1 = paramWaitingAudio.timecode;
	    long l2 = l1;
	    int j = 0;
	    int k = 0;
	    TSPacketFragment localRTPPacketFragment = (TSPacketFragment)paramWaitingAudio.fragments.remove(0);
	    int m = 0;
	    int n = localRTPPacketFragment.getOffset();
	    int i1 = localRTPPacketFragment.getLen();
	    byte[] arrayOfByte1 = localRTPPacketFragment.getBuffer();
	    int i2 = 1;
	    while (true)
	    {
	      int i3 = 0;
	      nextBlock();
	      this.block[i3] = 71;
	      i3++;
	      this.block[i3] = (byte)((i2 != 0 ? 64 : 0) + (0x1F & this.audioPID >> 8));
	      i3++;
	      this.block[i3] = (byte)(this.audioPID & 0xFF);
	      i3++;
	      if (this.audioCCounter == -1L)
	        this.audioCCounter = 1L;
	      else
	        this.audioCCounter += 1L;
	      this.block[i3] = (byte)(int)(16L + (this.audioCCounter & 0xF));
	      i3++;
	      int i4 = 0;
	      if (i2 != 0)
	        i4 = 9 + (j != 0 ? 10 : 5);
	      int i5 = 188 - i3 - i4;
	      if (i5 > i - k)
	        i5 = i - k;
	      int i6;
	      int i7;
	      long l3;
	      if (i3 + i5 + i4 < 188)
	      {
	        i6 = 188 - (i3 + i5 + i4);
	        int tmp642_641 = 3;
	        byte[] tmp642_633 = this.block;
	        tmp642_633[tmp642_641] = (byte)(tmp642_633[tmp642_641] | 0x20);
	        if (i6 > 1)
	        {
	          i6--;
	          this.block[i3] = (byte)(i6 & 0xFF);
	          i3++;
	          this.block[i3] = 0;
	          i3++;
	          i6--;
	          if (i6 > 0)
	            System.arraycopy(TransportStreamUtils.FILL, 0, this.block, i3, i6);
	          i3 += i6;
	        }
	        else
	        {
	          this.block[i3] = 0;
	          i3++;
	        }
	      }
	      if (i2 != 0)
	      {
	        this.block[i3] = 0;
	        i3++;
	        this.block[i3] = 0;
	        i3++;
	        this.block[i3] = 1;
	        i3++;
	        this.block[i3] = this.audioStreamID;
	        i3++;
	        i6 = j != 0 ? 10 : 5;
	        i7 = i + i6 + 3;
	        BufferUtils.intToByteArray(i7, this.block, i3, 2);
	        i3 += 2;
	        this.block[i3] = -128;
	        i3++;
	        this.block[i3] = (byte)(j != 0 ? 0xC0 : 0x80);
	        i3++;
	        this.block[i3] = (byte)i6;
	        i3++;
	        l3 = l2;
	        this.block[i3 + 4] = (byte)(int)(((l3 & 0x7F) << 1) + 1L);
	        l3 >>= 7;
	        this.block[i3 + 3] = (byte)(int)(l3 & 0xFF);
	        l3 >>= 8;
	        this.block[i3 + 2] = (byte)(int)(((l3 & 0x7F) << 1) + 1L);
	        l3 >>= 7;
	        this.block[i3 + 1] = (byte)(int)(l3 & 0xFF);
	        l3 >>= 8;
	        this.block[i3] = (byte)(int)(((l3 & 0x7) << 1) + 1L + (j != 0 ? 48 : 32));
	        i3 += 5;
	        if (j != 0)
	        {
	          l3 = l1;
	          this.block[i3 + 4] = (byte)(int)(((l3 & 0x7F) << 1) + 1L);
	          l3 >>= 7;
	          this.block[i3 + 3] = (byte)(int)(l3 & 0xFF);
	          l3 >>= 8;
	          this.block[i3 + 2] = (byte)(int)(((l3 & 0x7F) << 1) + 1L);
	          l3 >>= 7;
	          this.block[i3 + 1] = (byte)(int)(l3 & 0xFF);
	          l3 >>= 8;
	          this.block[i3] = (byte)(int)(((l3 & 0x7) << 1) + 1L + 32L);
	          i3 += 5;
	        }
	      }
	      while (true)
	      {
	        i6 = i5;
	        if (i6 > i1 - m)
	          i6 = i1 - m;
	        System.arraycopy(arrayOfByte1, n + m, this.block, i3, i6);
	        m += i6;
	        i3 += i6;
	        k += i6;
	        i5 -= i6;
	        if (m >= i1)
	        {
	          m = 0;
	          if (paramWaitingAudio.fragments.size() > 0)
	          {
	            localRTPPacketFragment = (TSPacketFragment)paramWaitingAudio.fragments.remove(0);
	            n = localRTPPacketFragment.getOffset();
	            i1 = localRTPPacketFragment.getLen();
	            arrayOfByte1 = localRTPPacketFragment.getBuffer();
	          }
	        }
	        if ((i3 >= 188) || (k >= i))
	          break;
	      }
	      i2 = 0;
	      this.isFirstAudioPacket = false;
	      os.write(block);
	      if (k >= i)
	        break;
	    }
	}
	
	private void handleVideo(ITag data) throws IOException {
		
		lastVideoTimecode = data.getTimestamp();
		IoBuffer dataBuff = data.getBody();
		long ts90 = data.getTimestamp() * 90;
		int dataLen = dataBuff.remaining();		
		byte[] dataBytes = dataBuff.array();
		H264CodecConfigInfo h264CodecInfo;
		if (dataBuff.remaining() >= 2) {
			
			int firstByte = dataBuff.get();
	        int secondByte = dataBuff.get();
	        int codec = (firstByte & 0x0F);
	        int frameType = firstByte >> 4 & 0x3;
	        int paloadLen = 0;
	        int naluNum = 0;
	        if ((codec == 7) && (secondByte != 1))  {
	        	if (secondByte == 0) {
	        		dataBuff.position(0);
	        		this.h264CodecConfigPart = H264Utils.breakApartAVCC(dataBuff);
	        		dataBuff.position(5);
	        		h264CodecInfo = H264Utils.decodeAVCC(dataBuff);		            
	        		System.out.println(h264CodecInfo);
	            }
	        } else if (codec == 7) {
	        	int cts = BufferUtils.byteArrayToInt(dataBytes, 2, 3) * 90;
	            long ts = ts90 + cts;
	            int i4 = 1;
	            int loop = 5;
	            int naluLen;
	            dataBuff.position(5);
	            ArrayList<TSPacketFragment> localArrayList = new ArrayList<TSPacketFragment>();
	            byte[] h264Startcode = new byte[4]; // h264 start code 0x00 0x00 0x00 0x01
	            h264Startcode[3] = 1;
	            int sps = 0;
	            int pps = 0;
	            int pd = 0;
	            
	            while (loop + 4 <= dataLen) {
	            	naluLen = BufferUtils.byteArrayToInt(dataBytes, loop, 4);
	            	loop += 4;
	            	
	            	if ((naluLen <= 0) || (loop + naluLen > dataLen))
	                    break;
	            	
	            	int naluType = dataBytes[loop] & 0x1F;
	            	if (naluType == 7) {  // sps
	            		sps = 1;
	            	} else if(naluType == 8) { // pps
	            		pps = 1;
	            	} else if(naluType == 9) { // pd
	            		pd = 1;
	            	}
	            		            	
	            	localArrayList.add(new TSPacketFragment(h264Startcode, 0, h264Startcode.length));
	            	localArrayList.add(new TSPacketFragment(dataBytes, loop, naluLen));
	            	paloadLen += (naluLen + h264Startcode.length);
	            	naluNum++;
	            	loop+=naluLen;
	            	
	            	if (loop >= dataLen)
	                    break;
	            }
	            
	            int idx = 0;
	            if(pd == 0) { 
	            	byte[] localObject2 = new byte[6];
	                localObject2[3] = 1;
	                localObject2[4] = 9;
	                if (frameType == 1)
	                  localObject2[5] = 16;
	                else if (frameType == 3)
	                  localObject2[5] = 80;
	                else
	                  localObject2[5] = 48;
	                localArrayList.add(idx, new TSPacketFragment(localObject2, 0, localObject2.length));
	                idx++;
	                paloadLen += localObject2.length;
	                naluNum++;
	                naluLen = 1;
	            } else {
	            	idx = 2;
	            }

	            if ((frameType == 1) && ((pps == 0) || (pd == 0))) {
	            	if ((frameType == 1) && (sps == 0) && (this.h264CodecConfigPart != null) && (this.h264CodecConfigPart.getSps() != null)) {
	            		byte[] localObject2 = new byte[4];
	                    localObject2[3] = 1;
	                    localArrayList.add(idx, new TSPacketFragment(localObject2, 0, localObject2.length));
	                    idx++;
	                    localArrayList.add(idx, new TSPacketFragment( this.h264CodecConfigPart.getSps(), 0, this.h264CodecConfigPart.getSps().length));
	                    idx++;
	                    paloadLen += this.h264CodecConfigPart.getSps().length + localObject2.length;
	                    naluNum++;
	                    naluLen = 1;
	            	}
	            	
	            	if ((frameType == 1) && (pps == 0) && (this.h264CodecConfigPart != null) && (this.h264CodecConfigPart.getPpss() != null)) {
	            		List<byte[]> localObject2 = this.h264CodecConfigPart.getPpss();
	                    for(byte[] b : localObject2)
	                    {
	                      byte[] arrayOfByte4 = new byte[4];
	                      arrayOfByte4[3] = 1;
	                      localArrayList.add(idx, new TSPacketFragment(arrayOfByte4, 0, arrayOfByte4.length));
	                      idx++;
	                      localArrayList.add(idx, new TSPacketFragment(b, 0, b.length));
	                      idx++;
	                      paloadLen += b.length + arrayOfByte4.length;
	                      naluNum++;
	                      naluLen = 1;
	            	}
	            }
	          }
	            
	            if (naluNum > 0) {
	            	TSPacketFragment localObject3 = (TSPacketFragment)localArrayList.remove(0);
	            	int maxPesDataLen = 32725;
	            	int offset = ((TSPacketFragment)localObject3).getOffset();
	                int len = ((TSPacketFragment)localObject3).getLen();
	                byte[] buff = ((TSPacketFragment)localObject3).getBuffer();
	                long pcr = getPCRTimecode();
	                int readedLen = 0;
	                int i20 = 1;
	                int i17 = 0;
	                while (true) {
	                	int unReadLen = paloadLen - readedLen;
	                	i20 = 1;
	                	int i23 = 1;
	                	int i24 = 0;
	                	if (unReadLen > maxPesDataLen)
	                		unReadLen = maxPesDataLen;
	                	
	                	 while (true) {
	                		 int tsIdx = 0;
	                		 nextBlock();
	                		 this.block[tsIdx] = 0x47;
	                		 tsIdx++;
	                		 this.block[tsIdx] = (byte)((i20 != 0 ? 64 : 0) + (0x1F & this.videoPID >> 8));
	                		 tsIdx++;
	                         this.block[tsIdx] = (byte)(this.videoPID & 0xFF);
	                         tsIdx++;
	                         if (this.videoCCounter == -1L)
	                           this.videoCCounter = 1L;
	                         else
	                           this.videoCCounter += 1L;
	                         this.block[tsIdx] = (byte)(int)(16L + (this.videoCCounter & 0xF));
	                         tsIdx++;
	                         
	                         int i27 = 0;
	                         if (i20 != 0)
	                           i27 = 9 + (i4 != 0 ? 10 : 5);
	                         int i28 = 188 - tsIdx - i27;
	                         if (i28 > unReadLen - i24)
	                           i28 = unReadLen - i24;
	                         int i29;
	                         int i30;
	                         long l5;
	                         
	                         if (i23 != 0) {
	                        	 int tmp1612_1611 = 3;
	                             byte[] tmp1612_1603 = this.block;
	                             tmp1612_1603[tmp1612_1611] = (byte)(tmp1612_1603[tmp1612_1611] | 0x20);
	                             i29 = 8;
	                             i28 = 188 - tsIdx - i27 - i29;
	                             if (i28 > unReadLen - i24)
	                               i28 = unReadLen - i24;
	                             i30 = 0;
	                             if (tsIdx + i28 + i27 + i29 < 188)
	                               i30 = 188 - (tsIdx + i28 + i27 + i29);
	                             this.block[tsIdx] = (byte)(i29 - 1 + i30 & 0xFF);
	                             tsIdx++;
	                             this.block[tsIdx] = (byte)((this.isFirstVideoPacket ? 0x80 : 16) | (frameType == 1 ? 64 : 0));
	                             tsIdx++;
	                             l5 = pcr;
	                             l5 <<= 7;
	                             byte[] arrayOfByte6 = BufferUtils.longToByteArray(l5);
	                             this.block[tsIdx + 4] = (byte)((arrayOfByte6[7] & 0x80) + 126);
	                             this.block[tsIdx + 3] = (byte)(arrayOfByte6[6] & 0xFF);
	                             this.block[tsIdx + 2] = (byte)(arrayOfByte6[5] & 0xFF);
	                             this.block[tsIdx + 1] = (byte)(arrayOfByte6[4] & 0xFF);
	                             this.block[tsIdx] = (byte)(arrayOfByte6[3] & 0xFF);
	                             tsIdx += 6;
	                             if (i30 > 0) {
	                               System.arraycopy(TransportStreamUtils.FILL, 0, this.block, tsIdx, i30);
	                               tsIdx += i30;
	                             }
	                             i28 = 188 - tsIdx - i27;
	                             if (i28 > unReadLen - i24)
	                               i28 = unReadLen - i24;
	                             i23 = 0;
	                         } else if (tsIdx + i28 + i27 < 188) {
	                        	 i29 = 188 - (tsIdx + i28 + i27);
	                             int tmp2007_2006 = 3;
	                             byte[] tmp2007_1998 = this.block;
	                             tmp2007_1998[tmp2007_2006] = (byte)(tmp2007_1998[tmp2007_2006] | 0x20);
	                             if (i29 > 1)
	                             {
	                               i29--;
	                               this.block[tsIdx] = (byte)(i29 & 0xFF);
	                               tsIdx++;
	                               this.block[tsIdx] = 0;
	                               tsIdx++;
	                               i29--;
	                               if (i29 > 0)
	                                 System.arraycopy(TransportStreamUtils.FILL, 0, this.block, tsIdx, i29);
	                               tsIdx += i29;
	                             }
	                             else
	                             {
	                               this.block[tsIdx] = 0;
	                               tsIdx++;
	                             }
	                         }
	                         if (i20 != 0)
	                         {
	                           this.block[tsIdx] = 0;
	                           tsIdx++;
	                           this.block[tsIdx] = 0;
	                           tsIdx++;
	                           this.block[tsIdx] = 1;
	                           tsIdx++;
	                           this.block[tsIdx] = this.videoStreamID;
	                           tsIdx++;
	                           i29 = i4 != 0 ? 10 : 5;
	                           i30 = unReadLen + i29 + 3;
	                           if (i30 >= 65536)
	                             System.out.println("toolong: " + i30);
	                           BufferUtils.intToByteArray(i30, this.block, tsIdx, 2);
	                           tsIdx += 2;
	                           this.block[tsIdx] = (byte)0x84;
	                           tsIdx++;
	                           this.block[tsIdx] = (byte)(i4 != 0 ? 0xC0 : 0x80);
	                           tsIdx++;
	                           this.block[tsIdx] = (byte)i29;
	                           tsIdx++;
	                           l5 = ts;
	                           this.block[tsIdx + 4] = (byte)(int)(((l5 & 0x7F) << 1) + 1L);
	                           l5 >>= 7;
	                           this.block[tsIdx + 3] = (byte)(int)(l5 & 0xFF);
	                           l5 >>= 8;
	                           this.block[tsIdx + 2] = (byte)(int)(((l5 & 0x7F) << 1) + 1L);
	                           l5 >>= 7;
	                           this.block[tsIdx + 1] = (byte)(int)(l5 & 0xFF);
	                           l5 >>= 8;
	                           this.block[tsIdx] = (byte)(int)(((l5 & 0x7) << 1) + 1L + (i4 != 0 ? 48 : 32));
	                           tsIdx += 5;
	                           if (i4 != 0)
	                           {
	                             l5 = ts90;//data.getTimestamp();
	                             this.block[tsIdx + 4] = (byte)(int)(((l5 & 0x7F) << 1) + 1L);
	                             l5 >>= 7;
	                             this.block[tsIdx + 3] = (byte)(int)(l5 & 0xFF);
	                             l5 >>= 8;
	                             this.block[tsIdx + 2] = (byte)(int)(((l5 & 0x7F) << 1) + 1L);
	                             l5 >>= 7;
	                             this.block[tsIdx + 1] = (byte)(int)(l5 & 0xFF);
	                             l5 >>= 8;
	                             this.block[tsIdx] = (byte)(int)(((l5 & 0x7) << 1) + 1L + (i4 != 0 ? 16 : 32));
	                             tsIdx += 5;
	                           }
	                         }
	                         
	                         while (true)
	                         {
	                           i29 = i28;
	                           if (i29 > len - i17)
	                             i29 = len - i17;
	                           System.arraycopy(buff, offset + i17, this.block, tsIdx, i29);
	                           i17 += i29;
	                           tsIdx += i29;
	                           readedLen += i29;
	                           i28 -= i29;
	                           i24 += i29;
	                           if (i17 >= len)
	                           {
	                             i17 = 0;
	                             if (localArrayList.size() > 0)
	                             {
	                               localObject3 = (TSPacketFragment)localArrayList.remove(0);
	                               offset = ((TSPacketFragment)localObject3).getOffset();
	                               len = ((TSPacketFragment)localObject3).getLen();
	                               buff = ((TSPacketFragment)localObject3).getBuffer();
	                             }
	                           }
	                           if ((tsIdx >= 188) || (i24 >= unReadLen) || (readedLen >= paloadLen))
	                             break;
	                         }
	                         i20 = 0;
	                         //System.err.println(HexDump.byteArrayToHexString(block, 0, 188).replace(":", " "));
	                         os.write(block);
	                         this.isFirstVideoPacket = false;
	                         if ((i24 >= unReadLen) || (readedLen >= paloadLen))
	                           break;
	                       }
	                       if (readedLen >= paloadLen)
	                         break;
	                	 }
	                }
		        }
	        }
	        
		}
	
	
	 private long getPCRTimecode()
	  {
	    long l = -1L;
	    if ((this.lastAudioTimecode >= 0L) && (this.lastVideoTimecode >= 0L))
	      l = Math.min(this.lastAudioTimecode, this.lastVideoTimecode);
	    else if (this.lastAudioTimecode >= 0L)
	      l = this.lastAudioTimecode;
	    else if (this.lastVideoTimecode >= 0L)
	      l = this.lastVideoTimecode;
	    if ((this.lastPCRTimecode != -1L) && (l < this.lastPCRTimecode))
	      l = this.lastPCRTimecode;
	    if (l < 0L)
	      l = 0L;
	    this.lastPCRTimecode = l;
	    return l;
	  }
	  
	public static void main(String[] args) throws Exception {
	
		File file = new File("d://lizee.flv");
		File ts = new File("d://123.ts");
		FileOutputStream fos = new FileOutputStream(ts);		
		FLVReader reader = new FLVReader(file);
		FLV2TSTest flv2ts = new FLV2TSTest(fos);
		ITag tag;
		while(reader.hasMoreTags()) {
			tag = reader.readTag();
			if(tag.getDataType() == 8) {
				flv2ts.handleAudio(tag);
			} else if(tag.getDataType() == 9) {
				flv2ts.handleVideo(tag);
			}
		}
		fos.close();
	}
	
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
			this.timecode = -1L;
			this.lastTimecode = -1L;
			this.count = 0;
			this.size = 0;
			this.fragments.clear();
		}

		public boolean isEmpty() {
			return this.size == 0;
		}

		public int size() {
			return this.size;
		}
	}
}
