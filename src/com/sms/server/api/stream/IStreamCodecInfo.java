package com.sms.server.api.stream;

/**
 * Stream codec information
 */
public interface IStreamCodecInfo {
    /**
     * Has audio support?
     * @return           <code>true</code> if stream codec has audio support, <code>false</code> otherwise
     */
    boolean hasAudio();

    /**
     * Has video support?
     * @return           <code>true</code> if stream codec has video support, <code>false</code> otherwise
     */
	boolean hasVideo();

	/**
     * Getter for audio codec name
     *
     * @return Audio codec name
     */
    String getAudioCodecName();

	/**
     * Getter for video codec name
     *
     * @return Video codec name
     */
    String getVideoCodecName();

	/**
     * Return video codec
     *
     * @return Video codec used by stream codec
     */
    IVideoStreamCodec getVideoCodec();
    
	/**
     * Return audio codec
     *
     * @return Audio codec used by stream codec
     */
    IAudioStreamCodec getAudioCodec();    
    
}
