package com.sms.server.stream.codec;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.api.stream.IAudioStreamCodec;

/**
 * 
 * @author pengliren
 *
 */
public class AudioStreamCodec implements IAudioStreamCodec {

	static final String CODEC_NAME = "AudioStreamCodec";
	
	private IoBuffer data;
	
	@Override
	public String getName() {
		
		return null;
	}

	@Override
	public void reset() {		
		
	}

	@Override
	public boolean canHandleData(IoBuffer data) {

		return false;
	}

	@Override
	public boolean addData(IoBuffer data) {
		this.data = data;
		return true;
	}

	@Override
	public IoBuffer getDecoderConfiguration() {
		return this.data;
	}

}
