package com.sms.server.stream.provider;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.IStreamableFile;
import com.sms.io.IStreamableFileFactory;
import com.sms.io.IStreamableFileService;
import com.sms.io.ITag;
import com.sms.io.ITagReader;
import com.sms.io.StreamableFileFactory;
import com.sms.io.flv.IKeyFrameDataAnalyzer;
import com.sms.io.flv.IKeyFrameDataAnalyzer.KeyFrameMeta;
import com.sms.server.api.IScope;
import com.sms.server.messaging.IMessage;
import com.sms.server.messaging.IMessageComponent;
import com.sms.server.messaging.IPassive;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.IPipeConnectionListener;
import com.sms.server.messaging.IPullableProvider;
import com.sms.server.messaging.OOBControlMessage;
import com.sms.server.messaging.PipeConnectionEvent;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.FlexStreamSend;
import com.sms.server.net.rtmp.event.IRTMPEvent;
import com.sms.server.net.rtmp.event.Invoke;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.event.Unknown;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.rtmp.message.Constants;
import com.sms.server.stream.ISeekableProvider;
import com.sms.server.stream.IStreamTypeAwareProvider;
import com.sms.server.stream.message.RTMPMessage;

/**
 * Pullable provider for files
 */
public class FileProvider implements IPassive, ISeekableProvider, IPullableProvider, IPipeConnectionListener, IStreamTypeAwareProvider {
	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(FileProvider.class);

	/**
	 * Class name
	 */
	public static final String KEY = FileProvider.class.getName();

	/**
	 * Provider scope
	 */
	@SuppressWarnings("unused")
	private IScope scope;

	/**
	 * Source file
	 */
	private File file;

	/**
	 * Consumer pipe
	 */
	private IPipe pipe;

	/**
	 * Tag reader
	 */
	private ITagReader reader;

	/**
	 * Keyframe metadata
	 */
	private KeyFrameMeta keyFrameMeta;

	/**
	 * Position at start
	 */
	private int start;

	/**
	 * Create file provider for given file and scope
	 * @param scope            Scope
	 * @param file             File
	 */
	public FileProvider(IScope scope, File file) {
		this.scope = scope;
		this.file = file;
	}

	/**
	 * Setter for start position
	 *
	 * @param start Start position
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/** {@inheritDoc} */
	public boolean hasVideo() {
		return (reader != null && reader.hasVideo());
	}

	/** {@inheritDoc} */
	public synchronized IMessage pullMessage(IPipe pipe) throws IOException {
		if (this.pipe == pipe) {
			if (this.reader == null) {
				init();
			}
			if (reader.hasMoreTags()) {
				IRTMPEvent msg = null;
				ITag tag = reader.readTag();
				if (tag != null) {
					int timestamp = tag.getTimestamp();
					switch (tag.getDataType()) {
						case Constants.TYPE_AUDIO_DATA:
							msg = new AudioData(tag.getBody());
							break;
						case Constants.TYPE_VIDEO_DATA:
							msg = new VideoData(tag.getBody());
							break;
						case Constants.TYPE_INVOKE:
							msg = new Invoke(tag.getBody());
							break;
						case Constants.TYPE_NOTIFY:
							msg = new Notify(tag.getBody());
							break;
						case Constants.TYPE_FLEX_STREAM_SEND:
							msg = new FlexStreamSend(tag.getBody());
							break;
						default:
							log.warn("Unexpected type? {}", tag.getDataType());
							msg = new Unknown(tag.getDataType(), tag.getBody());
					}
					msg.setTimestamp(timestamp);
					RTMPMessage rtmpMsg = RTMPMessage.build(msg);
					return rtmpMsg;
				} else {
					log.debug("Tag was null");
				}
			} else {
				// TODO send OOBCM to notify EOF
				// Do not unsubscribe if there aren't any more tags, as this kills VOD seek while in buffer
				// this.pipe.unsubscribe(this);
			}			
		}
		return null;
	}

	/** {@inheritDoc} */
	public IMessage pullMessage(IPipe pipe, long wait) throws IOException {
		return pullMessage(pipe);
	}

	/** {@inheritDoc} */
	public void onPipeConnectionEvent(PipeConnectionEvent event) {
		switch (event.getType()) {
			case PipeConnectionEvent.PROVIDER_CONNECT_PULL:
				if (pipe == null) {
					pipe = (IPipe) event.getSource();
				}
				break;
			case PipeConnectionEvent.PROVIDER_DISCONNECT:
				if (pipe == event.getSource()) {
					this.pipe = null;
					uninit();
				}
				break;
			case PipeConnectionEvent.CONSUMER_DISCONNECT:
				if (pipe == event.getSource()) {
					uninit();
				}
			default:
		}
	}

	/** {@inheritDoc} */
	public void onOOBControlMessage(IMessageComponent source, IPipe pipe, OOBControlMessage oobCtrlMsg) {
		String serviceName = oobCtrlMsg.getServiceName();
		String target = oobCtrlMsg.getTarget();
		log.debug("onOOBControlMessage - service name: {} target: {}", serviceName, target);
		if (serviceName != null) {
			if (IPassive.KEY.equals(target)) {
				if ("init".equals(serviceName)) {
					Integer startTS = (Integer) oobCtrlMsg.getServiceParamMap().get("startTS");
					setStart(startTS);
				}
			} else if (ISeekableProvider.KEY.equals(target)) {
				if ("seek".equals(serviceName)) {
					Integer position = (Integer) oobCtrlMsg.getServiceParamMap().get("position");
					int seekPos = seek(position.intValue());
					// Return position we seeked to
					oobCtrlMsg.setResult(seekPos);
				}
			} else if (IStreamTypeAwareProvider.KEY.equals(target)) {
				if ("hasVideo".equals(serviceName)) {
					oobCtrlMsg.setResult(hasVideo());
				}
			}
		}
	}

	/**
	 * Initializes file provider. Creates streamable file factory and service, seeks to start position
	 */
	private void init() throws IOException {
		IStreamableFileFactory factory = StreamableFileFactory.getInstance();
		IStreamableFileService service = factory.getService(file);
		if (service == null) {
			log.error("No service found for {}", file.getAbsolutePath());
			return;
		}
		IStreamableFile streamFile = service.getStreamableFile(file);
		reader = streamFile.getReader();
		if (start > 0) {
			seek(start);
		}
	}

	/**
	 * Reset
	 */
	private synchronized void uninit() {
		if (this.reader != null) {
			this.reader.close();
			this.reader = null;
		}
	}

	/** {@inheritDoc} */
	public synchronized int seek(int ts) {
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
		if (ts >= keyFrameMeta.duration) {
			// Seek at or beyond EOF
			reader.position(Long.MAX_VALUE);
			return (int) keyFrameMeta.duration;
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
