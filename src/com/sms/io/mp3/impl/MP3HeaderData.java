package com.sms.io.mp3.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MP3HeaderData {

	protected static Logger log = LoggerFactory.getLogger(MP3HeaderData.class);
	
	public static final byte INITIAL_SYNC = 0;
	public static final byte STRICT_SYNC = 1;
	
	public static final int		MPEG1 = 1;

	public static final int		STEREO = 0;
	public static final int		JOINT_STEREO = 1;
	public static final int		DUAL_CHANNEL = 2;
	public static final int		SINGLE_CHANNEL = 3;
	public static final int		FOURTYFOUR_POINT_ONE = 0;
	public static final int		FOURTYEIGHT=1;
	public static final int		THIRTYTWO=2;
	public static final int		MPEG2_LSF = 0;
	public static final int		MPEG25_LSF = 2;	// SZD

	public static final int[][]	frequencies =
	{{22050, 24000, 16000, 1},
	{44100, 48000, 32000, 1},
	{11025, 12000, 8000, 1}};	// SZD: MPEG25

	public static final int bitrates[][][] = {
		{{0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
	  112000, 128000, 144000, 160000, 176000, 192000 ,224000, 256000, 0},
	 	{0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
	  56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0},
	 	{0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
	  56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0}},

		{{0 /*free format*/, 32000, 64000, 96000, 128000, 160000, 192000,
	   224000, 256000, 288000, 320000, 352000, 384000, 416000, 448000, 0},
	  {0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
	   112000, 128000, 160000, 192000, 224000, 256000, 320000, 384000, 0},
	  {0 /*free format*/, 32000, 40000, 48000, 56000, 64000, 80000,
	   96000, 112000, 128000, 160000, 192000, 224000, 256000, 320000, 0}},
		// SZD: MPEG2.5
		{{0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
	  112000, 128000, 144000, 160000, 176000, 192000 ,224000, 256000, 0},
	 	{0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
	  56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0},
	 	{0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
	  56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0}},

		};

	public int				h_layer, h_protection_bit, h_bitrate_index,h_padding_bit, h_mode_extension;
	public int				h_version;
	public int				h_mode;
	public int				h_sample_frequency;
	public int				h_number_of_subbands, h_intensity_stereo_bound;
	public boolean			h_copyright, h_original;
	// VBR support added by E.B
	public double[] 		h_vbr_time_per_frame = {-1, 384, 1152, 1152};
	public int[] 			h_vbr_samples_per_frame = {-1, 384, 1152, 1152};
	public boolean			h_vbr;
	public int				h_vbr_frames;
	public int				h_vbr_scale;
	public int				h_vbr_bytes;
	public byte[]			h_vbr_toc;
		
	public short			checksum;
	public int				framesize;
	public int				nSlots;
	
	public boolean			single_ch_mode;
	public int 				syncword;
	
	//set_syncword(headerstring & 0xFFF80CC0);
	void set_syncword(int syncword0) {
		syncword = syncword0 & 0xFFFFFF3F;
		single_ch_mode = ((syncword0 & 0x000000C0) == 0x000000C0);
	}
	
	public int bitrate_instant() {
		int ret = 0;
		try {
			ret = bitrates[h_version][h_layer - 1][h_bitrate_index];
		} catch (Exception e) {
			log.error("bitrate_instant: h_version: " + h_version + " h_layer:"
					+ h_layer + " h_bitrate_index:" + h_bitrate_index
					+ " error:" + e.toString());
			ret = 0;
		}
		return ret;
	}
}
