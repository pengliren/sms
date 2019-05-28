package com.sms.server.stream.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.stream.IVideoStreamCodec;

/**
 * Red5 video codec for the AVC (h264) video format.
 *
 * Store DecoderConfigurationRecord and last keyframe (for now! we're cooking a very exciting new!)
 */
public class AVCVideo implements IVideoStreamCodec {

	private static Logger log = LoggerFactory.getLogger(AVCVideo.class);

	/**
	 * AVC video codec constant
	 */
	static final String CODEC_NAME = "AVC";

	/** Last keyframe found */
	private FrameData keyframe;
	
	/** Video decoder configuration data */
	private FrameData decoderConfiguration;

	/** Constructs a new AVCVideo. */
	public AVCVideo() {
		this.reset();
	}

	/** {@inheritDoc} */
	public String getName() {
		return CODEC_NAME;
	}

	/** {@inheritDoc} */
	public boolean canDropFrames() {
		return true;
	}

	/** {@inheritDoc} */
	public void reset() {
		keyframe = new FrameData();
		decoderConfiguration = new FrameData();
	}

	/** {@inheritDoc} */
	public boolean canHandleData(IoBuffer data) {
		if (data.limit() == 0) {
			// Empty buffer
			return false;
		}
		byte first = data.get();
		boolean result = ((first & 0x0f) == VideoCodec.AVC.getId());
		data.rewind();
		return result;
	}

	/** {@inheritDoc} */
	public boolean addData(IoBuffer data) {
		if (data.limit() > 0) {
			//ensure that we can "handle" the data
    		if (!canHandleData(data)) {
    			return false;
    		}
    		// get frame type
    		byte frameType = data.get();
    		// check for keyframe
    		if ((frameType & 0xf0) == FLV_FRAME_KEY) {
    			log.trace("Key frame found");
				byte AVCPacketType = data.get();
				// rewind
				data.rewind();
				// sequence header / here comes a AVCDecoderConfigurationRecord
				log.debug("AVCPacketType: {}", AVCPacketType);
				if (AVCPacketType == 0) {
					log.trace("Decoder configuration found");
					// Store AVCDecoderConfigurationRecord data
					decoderConfiguration.setData(data);
					// rewind
					data.rewind();
				}
   				// store last keyframe
   				keyframe.setData(data);
    		}
    		// finished with the data, rewind one last time
    		data.rewind();
		}
		return true;
	}

	/** {@inheritDoc} */
	public IoBuffer getKeyframe() {
		return keyframe.getFrame();
	}

	/** {@inheritDoc} */
	public IoBuffer getDecoderConfiguration() {
		return decoderConfiguration.getFrame();
	}
}
