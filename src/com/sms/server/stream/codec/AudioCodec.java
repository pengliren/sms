package com.sms.server.stream.codec;

/**
 * Audio codecs that Red5 supports.
 */
public enum AudioCodec {
	
	PCM((byte) 0), ADPCM((byte) 0x01), MP3((byte) 0x02), PCM_LE((byte) 0x03), 
	NELLY_MOSER_16K((byte) 0x04), NELLY_MOSER_8K((byte) 0x05), NELLY_MOSER((byte) 0x06), 
	PCM_ALAW((byte) 0x07), PCM_MULAW((byte) 0x08), RESERVED((byte) 0x09), 
	AAC((byte) 0x0a), SPEEX((byte) 0x0b), MP3_8K((byte) 0x0e), 
	DEVICE_SPECIFIC((byte) 0x0f);

	private byte id;

	private AudioCodec(byte id) {
		this.id = id;
	}

	/**
	 * Returns back a numeric id for this codec, that happens to correspond to the 
	 * numeric identifier that FLV will use for this codec.
	 * 
	 * @return the codec id
	 */
	public byte getId() {
		return id;
	}
	
}