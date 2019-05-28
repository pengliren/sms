package com.sms.server.media.h264;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.utils.BufferUtils;

/**
 * H264 Utils
 * @author pengliren
 *
 */
public class H264Utils {

	 private static Logger log = LoggerFactory.getLogger(H264Utils.class);
	
	 public static final int[][] pixel_aspect = { { 0, 1 }, { 1, 1 }, { 12, 11 }, { 10, 11 }, { 16, 11 }, { 40, 33 }, { 24, 11 }, { 20, 11 }, { 32, 11 }, { 80, 33 }, { 18, 11 }, { 15, 11 }, { 64, 33 }, { 160, 99 }, { 4, 3 }, { 3, 2 }, { 2, 1 } };
	 public static final byte[][] default_scaling4 = { { 6, 13, 20, 28, 13, 20, 28, 32, 20, 28, 32, 37, 28, 32, 37, 42 }, { 10, 14, 20, 24, 14, 20, 24, 27, 20, 24, 27, 30, 24, 27, 30, 34 } };
	 public static final byte[][] default_scaling8 = { { 6, 10, 13, 16, 18, 23, 25, 27, 10, 11, 16, 18, 23, 25, 27, 29, 13, 16, 18, 23, 25, 27, 29, 31, 16, 18, 23, 25, 27, 29, 31, 33, 18, 23, 25, 27, 29, 31, 33, 36, 23, 25, 27, 29, 31, 33, 36, 38, 25, 27, 29, 31, 33, 36, 38, 40, 27, 29, 31, 33, 36, 38, 40, 42 }, { 9, 13, 15, 17, 19, 21, 22, 24, 13, 13, 17, 19, 21, 22, 24, 25, 15, 17, 19, 21, 22, 24, 25, 27, 17, 19, 21, 22, 24, 25, 27, 28, 19, 21, 22, 24, 25, 27, 28, 30, 21, 22, 24, 25, 27, 28, 30, 32, 22, 24, 25, 27, 28, 30, 32, 33, 24, 25, 27, 28, 30, 32, 33, 35 } };
	 public static final byte[] zigzag_scan = { 0, 1, 4, 8, 5, 2, 3, 6, 9, 12, 13, 10, 7, 11, 14, 15 };
	 public static final byte[] zigzag_scan8x8 = { 0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63 };
	 
	public static H264CodecConfigInfo decodeAVCC(IoBuffer buff) {

		H264CodecConfigInfo h264CodecConfigInfo = new H264CodecConfigInfo();
		try {
			buff.skip(6);
			byte[] twoBytes = new byte[2];
			buff.get(twoBytes);
			int len = BufferUtils.byteArrayToInt(twoBytes, 0, 2);
			byte[] nals = new byte[len];
			buff.get(nals);
			byte[] arrayOfByte = nalUnescape(nals, 0, len);
			BitReader localBitReader = new BitReader(arrayOfByte);
			localBitReader.skip(8);
			h264CodecConfigInfo.profileIDC = localBitReader.getInt(8);
			localBitReader.skip(4);
			localBitReader.skip(4);
			h264CodecConfigInfo.levelIDC = localBitReader.getInt(8);
			h264CodecConfigInfo.spsID = localBitReader.readExpGolomb();
			if (h264CodecConfigInfo.profileIDC >= 100) {
				h264CodecConfigInfo.chromaFormatIDC = localBitReader.readExpGolomb();
				h264CodecConfigInfo.residualColorTransformFlag = 0;
				if (h264CodecConfigInfo.chromaFormatIDC == 3)
					h264CodecConfigInfo.residualColorTransformFlag = localBitReader.getInt(1);
				h264CodecConfigInfo.bitDepthLumaMinus8 = localBitReader.readExpGolomb();
				h264CodecConfigInfo.bitDepthChromaMinus8 = localBitReader.readExpGolomb();
				h264CodecConfigInfo.transformBypass = localBitReader.getInt(1);
				h264CodecConfigInfo.scalingMatrixFlag = localBitReader.getInt(1);
				if (h264CodecConfigInfo.scalingMatrixFlag == 1) {
					decode_scaling_list(localBitReader, 16, default_scaling4[0]);
					decode_scaling_list(localBitReader, 16, default_scaling4[0]);
					decode_scaling_list(localBitReader, 16, default_scaling4[0]);
					decode_scaling_list(localBitReader, 16, default_scaling4[1]);
					decode_scaling_list(localBitReader, 16, default_scaling4[1]);
					decode_scaling_list(localBitReader, 16, default_scaling4[1]);
					if (h264CodecConfigInfo.profileIDC >= 100) {
						decode_scaling_list(localBitReader, 64, default_scaling8[0]);
						decode_scaling_list(localBitReader, 64, default_scaling8[1]);
					}
				}
			}
			h264CodecConfigInfo.log2MaxFrameNum = (localBitReader.readExpGolomb() + 4);
			h264CodecConfigInfo.pocType = localBitReader.readExpGolomb();
			h264CodecConfigInfo.log2MaxPocLSB = 0;
			int k;
			if (h264CodecConfigInfo.pocType == 0) {
				h264CodecConfigInfo.log2MaxPocLSB = (localBitReader.readExpGolomb() + 4);
			} else if (h264CodecConfigInfo.pocType == 1) {
				h264CodecConfigInfo.deltaPicOrderAlwaysZeroFlag = localBitReader.getInt(1);
				h264CodecConfigInfo.offsetForNonRefPic = localBitReader.readExpGolombSigned();
				h264CodecConfigInfo.offsetForTopToBottomField = localBitReader.readExpGolombSigned();
				h264CodecConfigInfo.pocCycleLength = localBitReader.readExpGolomb();
				h264CodecConfigInfo.offsetForRefFrame = new int[h264CodecConfigInfo.pocCycleLength];
				for (k = 0; k < h264CodecConfigInfo.pocCycleLength; k++)
					h264CodecConfigInfo.offsetForRefFrame[k] = localBitReader.readExpGolombSigned();
			} else if (h264CodecConfigInfo.pocType == 2);
			h264CodecConfigInfo.refFrameCount = localBitReader.readExpGolomb();
			h264CodecConfigInfo.gapsInFrameNumAllowedFlag = localBitReader.getInt(1);
			h264CodecConfigInfo.mbWidth = (localBitReader.readExpGolomb() + 1);
			h264CodecConfigInfo.mbHeight = (localBitReader.readExpGolomb() + 1);
			h264CodecConfigInfo.frameMBSOnlyFlag = localBitReader.getInt(1);
			h264CodecConfigInfo.mbAFF = 0;
			if (h264CodecConfigInfo.frameMBSOnlyFlag == 0)
				h264CodecConfigInfo.mbAFF = localBitReader.getInt(1);
			h264CodecConfigInfo.adjWidth = h264CodecConfigInfo.mbWidth;
			h264CodecConfigInfo.adjHeight = (h264CodecConfigInfo.mbHeight * (2 - h264CodecConfigInfo.frameMBSOnlyFlag));
			h264CodecConfigInfo.direct8x8InferenceFlag = localBitReader.getInt(1);
			h264CodecConfigInfo.crop = localBitReader.getInt(1);
			h264CodecConfigInfo.cropLeft = 0;
			h264CodecConfigInfo.cropRight = 0;
			h264CodecConfigInfo.cropTop = 0;
			h264CodecConfigInfo.cropBottom = 0;
			if (h264CodecConfigInfo.crop != 0) {
				h264CodecConfigInfo.cropLeft = localBitReader.readExpGolomb();
				h264CodecConfigInfo.cropRight = localBitReader.readExpGolomb();
				h264CodecConfigInfo.cropTop = localBitReader.readExpGolomb();
				h264CodecConfigInfo.cropBottom = localBitReader.readExpGolomb();
			}
			h264CodecConfigInfo.vuiParametersPresentFlag = localBitReader.getInt(1);
			if (h264CodecConfigInfo.vuiParametersPresentFlag != 0) {
				h264CodecConfigInfo.sarNum = 0;
				h264CodecConfigInfo.sarDen = 0;
				h264CodecConfigInfo.aspectRatioIDC = 0;
				h264CodecConfigInfo.aspectRatioInfoPresentFlag = localBitReader.getInt(1);
				if (h264CodecConfigInfo.aspectRatioInfoPresentFlag != 0) {
					h264CodecConfigInfo.aspectRatioIDC = localBitReader.getInt(8);
					if (h264CodecConfigInfo.aspectRatioIDC == 255) {
						h264CodecConfigInfo.sarNum = localBitReader.getInt(16);
						h264CodecConfigInfo.sarDen = localBitReader.getInt(16);
					} else if (h264CodecConfigInfo.aspectRatioIDC < pixel_aspect.length) {
						h264CodecConfigInfo.sarNum = pixel_aspect[h264CodecConfigInfo.aspectRatioIDC][0];
						h264CodecConfigInfo.sarDen = pixel_aspect[h264CodecConfigInfo.aspectRatioIDC][1];
					}
				}
				k = localBitReader.getInt(1);
				if (k != 0)
					localBitReader.getInt(1);
				h264CodecConfigInfo.videoSignalTypePresentFlag = localBitReader.getInt(1);
				int i1;
				if (h264CodecConfigInfo.videoSignalTypePresentFlag != 0) {
					h264CodecConfigInfo.videoFormat = localBitReader.getInt(3);
					h264CodecConfigInfo.videoFullRange = localBitReader.getInt(1);
					int n = localBitReader.getInt(1);
					if (n != 0) {
						i1 = localBitReader.getInt(8);
						localBitReader.getInt(8);
						localBitReader.getInt(8);
					}
				}
				int n = localBitReader.getInt(1);
				if (n != 0) {
					i1 = localBitReader.readExpGolomb() + 1;
					localBitReader.readExpGolomb();
				}
				i1 = localBitReader.getInt(1);
				if (i1 != 0) {
					h264CodecConfigInfo.timingNumUnitsInTick = localBitReader.getLong(32);
					h264CodecConfigInfo.timingTimescale = localBitReader.getLong(32);
					h264CodecConfigInfo.timingFixedFrameRateFlag = localBitReader.getInt(1);
					if ((h264CodecConfigInfo.timingNumUnitsInTick > 0L) && (h264CodecConfigInfo.timingTimescale > 0L))
						h264CodecConfigInfo.frameRate = (Math.round(h264CodecConfigInfo.timingTimescale * 100L / (h264CodecConfigInfo.timingNumUnitsInTick * 2L)) / 100.0D);
				}
			}
			h264CodecConfigInfo.height = 0;
			h264CodecConfigInfo.width = (16 * h264CodecConfigInfo.mbWidth - 2 * (h264CodecConfigInfo.cropLeft + h264CodecConfigInfo.cropRight));
			if (h264CodecConfigInfo.frameMBSOnlyFlag != 0)
				h264CodecConfigInfo.height = (16 * h264CodecConfigInfo.adjHeight - 2 * (h264CodecConfigInfo.cropTop + h264CodecConfigInfo.cropBottom));
			else
				h264CodecConfigInfo.height = (16 * h264CodecConfigInfo.adjHeight - 4 * (h264CodecConfigInfo.cropTop + h264CodecConfigInfo.cropBottom));
			h264CodecConfigInfo.displayHeight = h264CodecConfigInfo.height;
			h264CodecConfigInfo.displayWidth = h264CodecConfigInfo.width;
			if ((h264CodecConfigInfo.sarNum > 0) && (h264CodecConfigInfo.sarDen > 0))
				h264CodecConfigInfo.displayWidth = (h264CodecConfigInfo.width * h264CodecConfigInfo.sarNum / h264CodecConfigInfo.sarDen);
		} catch (Exception exception) {
			log.info("H264CodecConfigInfo.decodeAVCC: " + exception.toString());
		}
		return h264CodecConfigInfo;
	}
	 
	public static H264CodecConfigParts breakApartAVCC(IoBuffer configure) {

		H264CodecConfigParts parts = new H264CodecConfigParts();
		configure.position(10);
		byte[] profileLevel = new byte[3];
		if (configure.remaining() > 3) {
			configure.skip(1);
			int len = configure.getShort() & 0xFFFF;
			if (len > configure.remaining()) return parts;
			byte[] sps = new byte[len];
			configure.get(sps);
			System.arraycopy(sps, 1, profileLevel, 0, 3);
			parts.setProfileLevel(profileLevel);
			parts.setSps(sps);
			
			byte[] pps;
			int ppsSize = configure.get();
			for(int i = 0; ((i < ppsSize) && (configure.remaining() > 2)); i++) {
				len = configure.getShort() & 0xFFFF;
				if (len > configure.remaining()) break; 
				pps = new byte[len];
				configure.get(pps);
				parts.addPPS(pps);
			}
		}
		return parts;
	}
	 
	static void decode_scaling_list(BitReader paramBitReader, int paramInt, byte[] paramArrayOfByte) {
		byte[] arrayOfByte = paramInt == 16 ? zigzag_scan : zigzag_scan8x8;
		int i = paramBitReader.getInt(1);
		if (i == 1) {
			int j = 8;
			int k = 8;
			for (int m = 0; m < paramInt; m++) {
				if (j != 0) {
					int n = paramBitReader.readExpGolombSigned();
					j = k + n & 0xFF;
				}
				if ((m == 0) && (j == 0))
					break;
				k = paramArrayOfByte[arrayOfByte[m]] = (byte) (j != 0 ? j : k);
			}
		}
	}
	 
	public static byte[] nalUnescape(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
		byte[] arrayOfByte1 = new byte[paramInt2];
		byte[] arrayOfByte2 = new byte[paramInt2 * 2];
		System.arraycopy(paramArrayOfByte, paramInt1, arrayOfByte1, 0, paramInt2);
		System.arraycopy(paramArrayOfByte, paramInt1, arrayOfByte2, 0, paramInt2);
		int i = 0;
		int j = 0;
		while (i + 2 < paramInt2) {
			if ((0xFF & arrayOfByte1[(i + 2)]) > 3) {
				arrayOfByte2[(j++)] = arrayOfByte1[(i++)];
				arrayOfByte2[(j++)] = arrayOfByte1[(i++)];
			} else if ((arrayOfByte1[i] == 0) && (arrayOfByte1[(i + 1)] == 0)
					&& (arrayOfByte1[(i + 2)] == 3)) {
				arrayOfByte2[(j++)] = 0;
				arrayOfByte2[(j++)] = 0;
				i += 3;
				continue;
			}
			arrayOfByte2[(j++)] = arrayOfByte1[(i++)];
		}
		return arrayOfByte2;
	}

	static String levelIDCToString(int paramInt) {
		String str = paramInt / 10 + "." + paramInt % 10;
		if (paramInt == 27)
			str = "1b";
		return str;
	}

	static String profileIDCToString(int paramInt) {
		String str = "" + paramInt;
		switch (paramInt) {
		case 66:
			str = "Baseline";
			break;
		case 77:
			str = "Main";
			break;
		case 88:
			str = "Extended";
			break;
		case 100:
			str = "High";
			break;
		case 110:
			str = "High 10 or High 10 Intra";
			break;
		case 122:
			str = "High 4:2:2 or High 4:2:2 Intra";
			break;
		case 244:
			str = "High 4:4:4 predictive or High 4:4:4 Intra";
			break;
		case 44:
			str = "CAVLC 4:4:4 Intra";
		}
		return str;
	}
}
