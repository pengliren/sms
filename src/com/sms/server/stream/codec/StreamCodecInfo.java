package com.sms.server.stream.codec;

import com.sms.server.api.stream.IAudioStreamCodec;
import com.sms.server.api.stream.IStreamCodecInfo;
import com.sms.server.api.stream.IVideoStreamCodec;

public class StreamCodecInfo implements IStreamCodecInfo {
	
	/**
	 * Audio support flag
	 */
	private boolean audio;

	/**
	 * Video support flag
	 */
	private boolean video;

	/**
	 * Audio codec
	 */
	private IAudioStreamCodec audioCodec;

	/**
	 * Video codec
	 */
	private IVideoStreamCodec videoCodec;	
	
	/** {@inheritDoc} */
	public boolean hasAudio() {
		return audio;
	}

	/**
	 * New value for audio support
	 *
	 * @param value Audio support
	 */
	public void setHasAudio(boolean value) {
		this.audio = value;
	}
	
	/** {@inheritDoc} */
	public String getAudioCodecName() {
		if (audioCodec == null) {
			return null;
		}
		return audioCodec.getName();
	}	
	
	/** {@inheritDoc} */
	public IAudioStreamCodec getAudioCodec() {
		return audioCodec;
	}

	/**
	 * Setter for audio codec
	 *
	 * @param codec Audio codec
	 */
	public void setAudioCodec(IAudioStreamCodec codec) {
		this.audioCodec = codec;
	}	

	/** {@inheritDoc} */
	public boolean hasVideo() {
		return video;
	}

	/**
	 * New value for video support
	 *
	 * @param value Video support
	 */
	public void setHasVideo(boolean value) {
		this.video = value;
	}

	/** {@inheritDoc} */
	public String getVideoCodecName() {
		if (videoCodec == null) {
			return null;
		}
		return videoCodec.getName();
	}

	/** {@inheritDoc} */
	public IVideoStreamCodec getVideoCodec() {
		return videoCodec;
	}

	/**
	 * Setter for video codec
	 *
	 * @param codec  Video codec
	 */
	public void setVideoCodec(IVideoStreamCodec codec) {
		this.videoCodec = codec;
	}
	
}
