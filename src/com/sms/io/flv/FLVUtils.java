package com.sms.io.flv;

/**
 * FLV Util
 * @author pengliren
 *
 */
public final class FLVUtils {

	public static final int FLV_CHUNKHEADER_ITYPE = 0;
	public static final int FLV_CHUNKHEADER_ISIZE = 1;
	public static final int FLV_CHUNKHEADER_ITIMECODE = 2;
	public static final int FLV_CHUNKHEADER_FIRSTBYTE = 3;
	public static final int FLV_CHUNKHEADER_SECONDBYTE = 4;
	public static final int FLV_CHUNKHEADER_HEADERSIZE = 11;
	public static final int FLV_CHUNKHEADER_BUFFERSIZE = 13;
	public static final int FLV_CHUNKHEADER_VALUESIZE = 5;
	public static final int FLV_UFRAME = 0;
	public static final int FLV_KFRAME = 1;
	public static final int FLV_DFRAME = 3;
	public static final int FLV_PFRAME = 2;
	public static final int FLV_TCINDEXAUDIO = 0;
	public static final int FLV_TCINDEXVIDEO = 1;
	public static final int FLV_TCINDEXDATA = 2;
	
	public static int getFrameType(int frame) {
		return frame >> 4 & 0x3;
	}

	public static int getAudioCodec(int audioCodec) {
		return audioCodec >> 4 & 0xF;
	}

	public static int getVideoCodec(int videoCodec) {
		return videoCodec & 0xF;
	}
}
