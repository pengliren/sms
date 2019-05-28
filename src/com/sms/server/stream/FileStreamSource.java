package com.sms.server.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.ITag;
import com.sms.io.ITagReader;
import com.sms.io.flv.IKeyFrameDataAnalyzer;
import com.sms.io.flv.IKeyFrameDataAnalyzer.KeyFrameMeta;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.IRTMPEvent;
import com.sms.server.net.rtmp.event.Invoke;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.event.Unknown;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.rtmp.message.Constants;

/**
 * Represents stream source that is file
 */
public class FileStreamSource implements ISeekableStreamSource, Constants {
    /**
     * Logger
     */
	protected static Logger log = LoggerFactory.getLogger(FileStreamSource.class);
    /**
     * Tag reader
     */
	private ITagReader reader;
    /**
     * Key frame metadata
     */
	private KeyFrameMeta keyFrameMeta;

    /**
     * Creates file stream source with tag reader
     * @param reader    Tag reader
     */
	public FileStreamSource(ITagReader reader) {
		this.reader = reader;
	}

    /**
     * Closes tag reader
     */
    public void close() {
		reader.close();
	}

    /**
     * Get tag from queue and convert to message
     * @return  RTMP event
     */
    public IRTMPEvent dequeue() {

		if (!reader.hasMoreTags()) {
			return null;
		}
		ITag tag = reader.readTag();

		IRTMPEvent msg;
		switch (tag.getDataType()) {
			case TYPE_AUDIO_DATA:
				msg = new AudioData(tag.getBody());
				break;
			case TYPE_VIDEO_DATA:
				msg = new VideoData(tag.getBody());
				break;
			case TYPE_INVOKE:
				msg = new Invoke(tag.getBody());
				break;
			case TYPE_NOTIFY:
				msg = new Notify(tag.getBody());
				break;
			default:
				log.warn("Unexpected type? {}", tag.getDataType());
				msg = new Unknown(tag.getDataType(), tag.getBody());
				break;
		}
		msg.setTimestamp(tag.getTimestamp());
		//msg.setSealed(true);
		return msg;
	}

	/** {@inheritDoc} */
    public boolean hasMore() {
		return reader.hasMoreTags();
	}

	/** {@inheritDoc} */
    public int seek(int ts) {
		log.trace("Seek ts: {}", ts);    	
		if (keyFrameMeta == null) {
			if (!(reader instanceof IKeyFrameDataAnalyzer)) {
				// Seeking not supported
				return ts;
			}

			keyFrameMeta = ((IKeyFrameDataAnalyzer) reader).analyzeKeyFrames();
		}

		if (keyFrameMeta.positions.length == 0) {
			// no video keyframe metainfo, it's an audio-only FLV
			// we skip the seek for now.
			// TODO add audio-seek capability
			return ts;
		}
		
		int frame = 0;
		for (int i = 0; i < keyFrameMeta.positions.length; i++) {
			if (keyFrameMeta.timestamps[i] > ts) {
				break;
			}
			frame = i;
		}
		reader.position(keyFrameMeta.positions[frame]);
		return keyFrameMeta.timestamps[frame];
	}
}
