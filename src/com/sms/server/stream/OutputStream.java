package com.sms.server.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.net.rtmp.Channel;

/**
 * Output stream that consists of audio, video and data channels
 */
public class OutputStream {
    /**
     * Logger
     */
	protected static Logger log = LoggerFactory.getLogger(OutputStream.class);
    /**
     * Video channel
     */
	private Channel video;
    /**
     * Audio channel
     */
	private Channel audio;
    /**
     * Data channel
     */
	private Channel data;

    /**
     * Creates output stream from channels
     *
     * @param video        Video channel
     * @param audio        Audio channel
     * @param data         Data channel
     */
    public OutputStream(Channel video, Channel audio, Channel data) {
		this.video = video;
		this.audio = audio;
		this.data = data;
	}

    /**
     * Closes audion, video and data channels
     */
    public void close() {
		this.video.close();
		this.audio.close();
		this.data.close();
	}

	/**
     * Getter for audio channel
     *
     * @return  Audio channel
     */
    public Channel getAudio() {
		return audio;
	}

	/**
     * Getter for data channel
     *
     * @return   Data channel
     */
    public Channel getData() {
		return data;
	}

	/**
     * Getter for video channel
     *
     * @return Video channel
     */
    public Channel getVideo() {
		return video;
	}
}
