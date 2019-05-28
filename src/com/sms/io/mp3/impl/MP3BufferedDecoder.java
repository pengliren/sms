package com.sms.io.mp3.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MP3BufferedDecoder {

	protected static Logger log = LoggerFactory.getLogger(MP3BufferedDecoder.class);
	
	public static int frequency(MP3HeaderData headerData) {return MP3HeaderData.frequencies[headerData.h_version][headerData.h_sample_frequency];}

	public static int samples_per_frame(MP3HeaderData headerData)
	{
		int ret = 0;
		try
		{
			if (headerData.h_vbr == true)
			{			
				ret = headerData.h_vbr_samples_per_frame[headerData.h_layer];
				if ((headerData.h_version == MP3HeaderData.MPEG2_LSF) || (headerData.h_version == MP3HeaderData.MPEG25_LSF)) ret /= 2;
			}
			else
			{
				// {{22050, 24000, 16000},
				// {44100, 48000, 32000},
				// {11025, 12000, 8000}};	// SZD: MPEG25
				//public int frequency() {return frequencies[h_version][h_sample_frequency];}
	
				int samples_per_frame_array[][][] = 
						{
						// 22050, 24000, 16000
						//{8.707483f,  8.0f, 12.0f}
						//{26.12245f, 24.0f, 36.0f}
						//{26.12245f, 24.0f, 36.0f}
						{{192, 192, 192},
						 {576, 576, 576},
						 {576, 576, 576}},
						
						// {44100, 48000, 32000},
						//{8.707483f,  8.0f, 12.0f}
						//{26.12245f, 24.0f, 36.0f}
						//{26.12245f, 24.0f, 36.0f}
						{{384,  384,  384},
						 {1152, 1152, 1152},
						 {1152, 1152, 1152}},
						
						// {11025, 12000, 8000}};	// SZD: MPEG25
						//{8.707483f,  8.0f, 12.0f}
						//{26.12245f, 24.0f, 36.0f}
						//{26.12245f, 24.0f, 36.0f}
						{{96,  96,  96},
						 {288, 288, 288},
						 {288, 288, 288}}};
					
				return(samples_per_frame_array[headerData.h_version][headerData.h_layer-1][headerData.h_sample_frequency]);
			}
		}
		catch (Exception e)
		{
			log.error("MP3BufferedDecoder.samples_per_frame: h_version: "+headerData.h_version+" h_layer:"+headerData.h_layer+" h_sample_frequency:"+headerData.h_sample_frequency+" error:"+e.toString());
		}
		
		return ret;
	}

	public static int syncHeader(byte syncmode, byte[] fourBytes, MP3HeaderData headerData)
	{
		boolean sync;
		int headerstring;

		headerstring = ((fourBytes[0] << 16) & 0x00FF0000) | ((fourBytes[1] << 8) & 0x0000FF00) | ((fourBytes[2] << 0) & 0x000000FF);
		headerstring <<= 8;
		headerstring |= (fourBytes[3] & 0x000000FF);

		sync = isSyncMark(headerstring, syncmode, headerData);

		return sync?headerstring:0;
	}
	
	public static boolean isSyncMark(int headerstring, int syncmode, MP3HeaderData headerData)
	{
		int word = headerData.syncword;
		boolean sync = false;

		if (syncmode == MP3HeaderData.INITIAL_SYNC)
		{
			//sync =  ((headerstring & 0xFFF00000) == 0xFFF00000);
			sync =  ((headerstring & 0xFFE00000) == 0xFFE00000);	// SZD: MPEG 2.5
		}
		else
		{
			sync =  ((headerstring & 0xFFF80C00) == word) &&
			    (((headerstring & 0x000000C0) == 0x000000C0) == headerData.single_ch_mode);
		}

		// filter out invalid sample rate
		if (sync)
			sync = (((headerstring >>> 10) & 3)!=3);
		// filter out invalid layer
		if (sync)
			sync = (((headerstring >>> 17) & 3)!=0);
		// filter out invalid version
		if (sync)
			sync = (((headerstring >>> 19) & 3)!=1);

		return sync;
	}
	
	public static void decodeHeader(int headerstring, int syncmode, MP3HeaderData headerData)
	{
		try
		{
			int channel_bitrate;
			if (syncmode == MP3HeaderData.INITIAL_SYNC)
			{
				headerData.h_version = ((headerstring >>> 19) & 1);
				if (((headerstring >>> 20) & 1) == 0) // SZD: MPEG2.5 detection
					if (headerData.h_version == MP3HeaderData.MPEG2_LSF)
						headerData.h_version = MP3HeaderData.MPEG25_LSF;
					else
					{
						//TODO problem
					}
				if ((headerData.h_sample_frequency = ((headerstring >>> 10) & 3)) == 3)
				{
					//TODO problem
				}
			}
			headerData.h_layer = 4 - (headerstring >>> 17) & 3;
			headerData.h_protection_bit = (headerstring >>> 16) & 1;
			headerData.h_bitrate_index = (headerstring >>> 12) & 0xF;
			headerData.h_padding_bit = (headerstring >>> 9) & 1;
			headerData.h_mode = ((headerstring >>> 6) & 3);
			headerData.h_mode_extension = (headerstring >>> 4) & 3;
			if (headerData.h_mode == MP3HeaderData.JOINT_STEREO)
				headerData.h_intensity_stereo_bound = (headerData.h_mode_extension << 2) + 4;
			else
				headerData.h_intensity_stereo_bound = 0; // should never be used
			if (((headerstring >>> 3) & 1) == 1)
				headerData.h_copyright = true;
			if (((headerstring >>> 2) & 1) == 1)
				headerData.h_original = true;
			// calculate number of subbands:
			if (headerData.h_layer == 1)
				headerData.h_number_of_subbands = 32;
			else
			{
				channel_bitrate = headerData.h_bitrate_index;
				// calculate bitrate per channel:
				if (headerData.h_mode != MP3HeaderData.SINGLE_CHANNEL)
					if (channel_bitrate == 4)
						channel_bitrate = 1;
					else
						channel_bitrate -= 4;
				if ((channel_bitrate == 1) || (channel_bitrate == 2))
					if (headerData.h_sample_frequency == MP3HeaderData.THIRTYTWO)
						headerData.h_number_of_subbands = 12;
					else
						headerData.h_number_of_subbands = 8;
				else if ((headerData.h_sample_frequency == MP3HeaderData.FOURTYEIGHT) || ((channel_bitrate >= 3) && (channel_bitrate <= 5)))
					headerData.h_number_of_subbands = 27;
				else
					headerData.h_number_of_subbands = 30;
			}
			if (headerData.h_intensity_stereo_bound > headerData.h_number_of_subbands)
				headerData.h_intensity_stereo_bound = headerData.h_number_of_subbands;
		}
		catch (Exception e)
		{
			log.error("MP3BufferedDecoder.decodeHeader:"+e.toString());
			e.printStackTrace();
		}
	}
	
	public static int calculateFrameSize(MP3HeaderData headerData)
	{
		int framesize = 0;
		@SuppressWarnings("unused")
		int nSlots;
		
		 if (headerData.h_layer == 1)
		 {
		   framesize = (12 * MP3HeaderData.bitrates[headerData.h_version][0][headerData.h_bitrate_index]) /
		   MP3HeaderData.frequencies[headerData.h_version][headerData.h_sample_frequency];
		   if (headerData.h_padding_bit != 0 ) framesize++;
		   framesize <<= 2;		// one slot is 4 bytes long
		   nSlots = 0;
		 }
		 else
		 {
		   framesize = (144 * MP3HeaderData.bitrates[headerData.h_version][headerData.h_layer - 1][headerData.h_bitrate_index]) /
		   MP3HeaderData.frequencies[headerData.h_version][headerData.h_sample_frequency];
		   if (headerData.h_version == MP3HeaderData.MPEG2_LSF || headerData.h_version == MP3HeaderData.MPEG25_LSF) framesize >>= 1;	// SZD
		   if (headerData.h_padding_bit != 0) framesize++;
		   // Layer III slots
		   if (headerData.h_layer == 3)
		   {
		     if (headerData.h_version == MP3HeaderData.MPEG1)
		     {
		  		 nSlots = framesize - ((headerData.h_mode == MP3HeaderData.SINGLE_CHANNEL) ? 17 : 32) // side info size
		  								  -  ((headerData.h_protection_bit!=0) ? 0 : 2) 		       // CRC size
		  								  - 4; 								             // header size
		     }
		     else
			 {  // MPEG-2 LSF, SZD: MPEG-2.5 LSF
		        nSlots = framesize - ((headerData.h_mode == MP3HeaderData.SINGLE_CHANNEL) ?  9 : 17) // side info size
		  					   		  -  ((headerData.h_protection_bit!=0) ? 0 : 2) 		       // CRC size
		  								  - 4; 								             // header size
		     }
		   }
		   else
		   {
		  	 nSlots = 0;
		   }
		 }
		 framesize -= 4;             // subtract header size
		 return framesize;
	}
}
