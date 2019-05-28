
package com.sms.server.stream.codec;
import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.stream.IAudioStreamCodec;

/**
 * Red5 audio codec for the AAC audio format.
 *
 * Stores the decoder configuration
 */
public class AACAudio implements IAudioStreamCodec {

	private static Logger log = LoggerFactory.getLogger(AACAudio.class);

	public static final int[] AAC_SAMPLERATES = { 96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050, 16000, 12000, 11025, 8000, 7350 };

	/**
	 * AAC audio codec constant
	 */
	static final String CODEC_NAME = "AAC";

	/**
	 * Block of data (AAC DecoderConfigurationRecord)
	 */
	private byte[] blockDataAACDCR;

	/** Constructs a new AVCVideo. */
	public AACAudio() {
		this.reset();
	}

	/** {@inheritDoc} */
	public String getName() {
		return CODEC_NAME;
	}

	/** {@inheritDoc} */
	public void reset() {
		blockDataAACDCR = null;
	}

	/** {@inheritDoc} */
	public boolean canHandleData(IoBuffer data) {
		if (data.limit() == 0) {
			// Empty buffer
			return false;
		}
		byte first = data.get();
		boolean result = (((first & 0xf0) >> 4) == AudioCodec.AAC.getId());
		data.rewind();
		return result;
	}

	/** {@inheritDoc} */
	public boolean addData(IoBuffer data) {
		int dataLength = data.limit();
		if (dataLength > 1) {
			//ensure we are at the beginning
			data.rewind();
			byte frameType = data.get();
			log.trace("Frame type: {}", frameType);
			byte header = data.get();
			//go back to beginning
			data.rewind();
			//If we don't have the AACDecoderConfigurationRecord stored...
			if (blockDataAACDCR == null) {
				if ((((frameType & 0xF0) >> 4) == AudioCodec.AAC.getId()) && (header == 0)) {
					//go back to beginning
					data.rewind();
					blockDataAACDCR = new byte[dataLength];
					data.get(blockDataAACDCR);
					//go back to beginning
					data.rewind();
				}
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	public IoBuffer getDecoderConfiguration() {
		if (blockDataAACDCR == null) {
			return null;
		}
		IoBuffer result = IoBuffer.allocate(4);
		result.setAutoExpand(true);
		result.put(blockDataAACDCR);
		result.rewind();
		return result;
	}

	@SuppressWarnings("unused")
	private long sample2TC(long time, int sampleRate) {
		return (time * 1000L / sampleRate);
	}

	//private final byte[] getAACSpecificConfig() {		
	//	byte[] b = new byte[] { 
	//			(byte) (0x10 | /*((profile > 2) ? 2 : profile << 3) | */((sampleRateIndex >> 1) & 0x03)),
	//			(byte) (((sampleRateIndex & 0x01) << 7) | ((channels & 0x0F) << 3))
	//		};
	//	log.debug("SpecificAudioConfig {}", HexDump.toHexString(b));
	//	return b;	
	//}    
}
