package com.sms.server.stream.codec;


/**
 * Video codecs that Red5 supports.
 */
public enum VideoCodec {
	
	JPEG((byte) 0x01), H263((byte) 0x02), SCREEN_VIDEO((byte) 0x03), VP6((byte) 0x04),
	VP6a((byte) 0x05), SCREEN_VIDEO2((byte) 0x06), AVC((byte) 0x07);

	private byte id;

	private VideoCodec(byte id) {
		this.id = id;
	}

	/**
	 * Returns back a numeric id for this codec,
	 * that happens to correspond to the numeric
	 * identifier that FLV will use for this codec.
	 * 
	 * @return the codec id
	 */
	public byte getId() {
		return id;
	}
	
}