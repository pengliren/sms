package com.sms.server.media.aac;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * AAC Utils
 * @author pengliren
 *
 */
public class AACUtils {

	public static final int AAC_HEADER_SIZE = 7;
	public static final int[] AAC_SAMPLERATES = { 96000, 88200, 64000, 48000,44100, 32000, 24000, 22050, 16000, 12000, 11025, 8000, 7350 };
	public static final int[] AAC_CHANNELS = { 0, 1, 2, 3, 4, 5, 6, 8 };

	public static String profileObjectTypeToString(int profileType) {
		String str = "Unknown[" + profileType + "]";
		switch (profileType) {
		case 0:
			str = "NULL[0]";
			break;
		case 1:
			str = "Main";
			break;
		case 2:
			str = "LC";
			break;
		case 3:
			str = "SBR";
			break;
		case 4:
			str = "LongTermPrediction";
			break;
		case 5:
			str = "HE";
			break;
		case 6:
			str = "Scalable";
			break;
		case 7:
			str = "TwinVQ";
			break;
		case 8:
			str = "CELP";
			break;
		case 9:
			str = "HVXC";
			break;
		case 10:
			str = "Reserved[10]";
			break;
		case 11:
			str = "Reserved[11]";
			break;
		case 12:
			str = "TTSI";
			break;
		case 13:
			str = "Synthetic";
			break;
		case 14:
			str = "WavetableSynthesis";
			break;
		case 15:
			str = "GeneralMIDI";
			break;
		case 16:
			str = "AlgorithmicSynthesisAndAudioFX";
			break;
		case 17:
			str = "LowComplexityWithErrorRecovery";
			break;
		case 18:
			str = "Reserved[18]";
			break;
		case 19:
			str = "LongTermPredictionWithErrorRecover";
			break;
		case 20:
			str = "ScalableWithErrorRecovery";
			break;
		case 21:
			str = "TwinVQWithErrorRecovery";
			break;
		case 22:
			str = "BSACWithErrorRecovery";
			break;
		case 23:
			str = "LDWithErrorRecovery";
			break;
		case 24:
			str = "CELPWithErrorRecovery";
			break;
		case 25:
			str = "HXVCWithErrorRecovery";
			break;
		case 26:
			str = "HILNWithErrorRecovery";
			break;
		case 27:
			str = "ParametricWithErrorRecovery";
			break;
		case 28:
			str = "Reserved[28]";
			break;
		case 29:
			str = "Reserved[29]";
			break;
		case 30:
			str = "Reserved[30]";
			break;
		case 31:
			str = "Reserved[31]";
		}
		return str;
	}

	public static int sampleRateToIndex(int rate) {
		int rateIdx = 0;
		for (int i = 0; i < AAC_SAMPLERATES.length; i++) {
			if (rate != AAC_SAMPLERATES[i])
				continue;
			rateIdx = i;
			break;
		}
		return rateIdx;
	}
	
	public static AACFrame decodeFrame(IoBuffer data) {

		if(data.remaining() < 7) return null;
		AACFrame aacFrame = null;
		byte first = data.get();
		byte second = data.get();
		byte third = data.get();
		if ((first & 0xFF) == 0xFF && (second & 0xF0) == 0xF0) {
			int rate = 0;
			int channels = 0;
			boolean errorBitsAbsent = (second & 0x1) == 1;
			int profileType = (third >> 6 & 0x3) + 1;
			int rateIdx = third & 0x3C;
			rateIdx >>= 2;
			if (rateIdx >= 0 && rateIdx < AAC_SAMPLERATES.length) {
				rate = AAC_SAMPLERATES[rateIdx];
				int channelIdx = third & 0x1;
				channelIdx <<= 2;
				byte fourth = data.get();
				channelIdx += ((byte) (fourth >> 6) & 0x3);
				if (channelIdx >= 0 && channelIdx < AAC_CHANNELS.length) {
					channels = AAC_CHANNELS[channelIdx];
					int size = fourth & 0x3;
					size <<= 8;
					size += (data.get() & 0xFF);
					size <<= 3;
					size += (data.get() >> 5 & 0x7);
					if (size > 0) {
						int rdb = data.get() & 0x3;
						aacFrame = new AACFrame();
						aacFrame.setChannelIndex(channelIdx);
						aacFrame.setChannels(channels);
						aacFrame.setRdb(rdb);
						aacFrame.setRateIndex(rateIdx);
						aacFrame.setSampleRate(rate);
						aacFrame.setSize(size);
						int dataLen = size - 7;// adts header 7 byte
						if(data.remaining() >= dataLen) {
							byte[] buffer = new byte[dataLen]; 
							data.get(buffer);
							aacFrame.setData(IoBuffer.wrap(buffer));
						}
						aacFrame.setErrorBitsAbsent(errorBitsAbsent);
						aacFrame.setProfileObjectType(profileType);
					}
				}
			}
		}
		return aacFrame;
	}
	
	public static AACFrame decodeAACCodecConfig(IoBuffer buffer) {
		
		// aacCodecConfig need 2 byte
		if (buffer.remaining() < 2) return null;
		byte first = buffer.get();
		byte second = buffer.get();
		int profileType = (first & 0xf8) >> 3;
        int rateIdx = (first & 7) << 1;
        rateIdx += (second & 0x80) >> 7 & 1;
        int channelIdx = (second & 0x7f) >> 3;
        int rate = 0;
        int channels = 0;
        if(rateIdx >= 0 && rateIdx < AAC_SAMPLERATES.length)
        	rate = AAC_SAMPLERATES[rateIdx];
        if(channelIdx >= 0 && channelIdx < AAC_CHANNELS.length)
        	channels = AAC_CHANNELS[channelIdx];
        AACFrame aacframe = null;
        aacframe = new AACFrame();
        aacframe.setProfileObjectType(profileType);
        aacframe.setChannelIndex(channelIdx);
        aacframe.setChannels(channels);
        aacframe.setRateIndex(rateIdx);
        aacframe.setSampleRate(rate);
        return aacframe;
	}
	
	public static IoBuffer frameToADTSBuffer(AACFrame aacFrame) {

		byte[] adts = new byte[7];
		frameToADTSBuffer(aacFrame, adts, 0);
		return IoBuffer.wrap(adts);
	}
	
	/**
	 * Letter 	Length (bits) 	Description
		A 		12 	syncword 0xFFF, all bits must be 1
		B 		1 	MPEG Version: 0 for MPEG-4, 1 for MPEG-2
		C 		2 	Layer: always 0
		D 		1 	protection absent, Warning, set to 1 if there is no CRC and 0 if there is CRC
		E 		2 	profile, the MPEG-4 Audio Object Type minus 1
		F 		4 	MPEG-4 Sampling Frequency Index (15 is forbidden)
		G 		1 	private stream, set to 0 when encoding, ignore when decoding
		H 		3 	MPEG-4 Channel Configuration (in the case of 0, the channel configuration is sent via an inband PCE)
		I 		1 	originality, set to 0 when encoding, ignore when decoding
		J 		1 	home, set to 0 when encoding, ignore when decoding
		K 		1 	copyrighted stream, set to 0 when encoding, ignore when decoding
		L 		1 	copyright start, set to 0 when encoding, ignore when decoding
		M 		13 	frame length, this value must include 7 or 9 bytes of header length: FrameLength = (ProtectionAbsent == 1 ? 7 : 9) + size(AACFrame)
		O 		11 	Buffer fullness
		P 		2 	Number of AAC frames (RDBs) in ADTS frame minus 1, for maximum compatibility always use 1 AAC frame per ADTS frame
		Q 		16 	CRC if protection absent is 0 
	 * @param aacFrame
	 * @param adts
	 * @param startIdx
	 */
	public static void frameToADTSBuffer(AACFrame aacFrame, byte[] adts, int startIdx) {
		
		int rateIdx = aacFrame.getRateIndex();
	    int profileObjectType = aacFrame.getProfileObjectType();
	    int channelIdx = aacFrame.getChannelIndex();
	    int size = aacFrame.getSize();
	    boolean isError = aacFrame.isErrorBitsAbsent();
	    adts[(startIdx + 0)] = (byte)0xFF;
	    adts[(startIdx + 1)] = isError ? (byte) 0xF1 : (byte) 0xF0;
	    adts[(startIdx + 2)] = (byte)((profileObjectType - 1 & 0x3) << 6);
	    int adtsSecondByte = (startIdx + 2);
	    adts[adtsSecondByte] = (byte)(adts[adtsSecondByte] + (byte)(rateIdx << 2 & 0x3C));
	    adts[adtsSecondByte] = (byte)(adts[adtsSecondByte] + (byte)(channelIdx >> 2 & 0x1));
	    adts[(startIdx + 3)] = (byte)((channelIdx & 0x3) << 6);
	    adts[(startIdx + 5)] = (byte)((size & 0x7) << 5);
	    size >>= 3;
	    adts[(startIdx + 4)] = (byte)(size & 0xFF);
	    size >>= 8;
	    int adtsThirdByte = (startIdx + 3);
	    adts[adtsThirdByte] = (byte)(adts[adtsThirdByte] + (byte)(size & 0x3));
	    int n = 2047;
	    int adtsFiveByte = (startIdx + 5);
	    adts[adtsFiveByte] = (byte)(adts[adtsFiveByte] + (n >> 6));
	    adts[(startIdx + 6)] = (byte)(n << 2 & 0xFF);
	    int rdb = aacFrame.getRdb();
	    int adtsSixByte = (startIdx + 6);
	    adts[adtsSixByte] = (byte)(adts[adtsSixByte] + (byte)(rdb & 0x3));
	}
}
