package com.sms.server.stream;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.management.ObjectName;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.jmx.JMXAgent;
import com.sms.jmx.JMXFactory;
import com.sms.jmx.mxbeans.ClientBroadcastStreamMXBean;
import com.sms.server.ScopeContextBean;
import com.sms.server.api.IConnection;
import com.sms.server.api.IScope;
import com.sms.server.api.SMS;
import com.sms.server.api.ScopeUtils;
import com.sms.server.api.event.IEvent;
import com.sms.server.api.event.IEventDispatcher;
import com.sms.server.api.event.IEventListener;
import com.sms.server.api.statistics.IClientBroadcastStreamStatistics;
import com.sms.server.api.statistics.StatisticsCounter;
import com.sms.server.api.stream.IAudioStreamCodec;
import com.sms.server.api.stream.IClientBroadcastStream;
import com.sms.server.api.stream.IStreamAwareScopeHandler;
import com.sms.server.api.stream.IStreamCapableConnection;
import com.sms.server.api.stream.IStreamCodecInfo;
import com.sms.server.api.stream.IStreamFilenameGenerator;
import com.sms.server.api.stream.IStreamListener;
import com.sms.server.api.stream.IStreamPacket;
import com.sms.server.api.stream.IVideoStreamCodec;
import com.sms.server.api.stream.ResourceExistException;
import com.sms.server.api.stream.ResourceNotFoundException;
import com.sms.server.api.stream.IStreamFilenameGenerator.GenerationType;
import com.sms.server.messaging.AbstractPipe;
import com.sms.server.messaging.IConsumer;
import com.sms.server.messaging.IFilter;
import com.sms.server.messaging.IMessage;
import com.sms.server.messaging.IMessageComponent;
import com.sms.server.messaging.IMessageOutput;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.IPipeConnectionListener;
import com.sms.server.messaging.IProvider;
import com.sms.server.messaging.IPushableConsumer;
import com.sms.server.messaging.InMemoryPushPushPipe;
import com.sms.server.messaging.OOBControlMessage;
import com.sms.server.messaging.PipeConnectionEvent;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.IRTMPEvent;
import com.sms.server.net.rtmp.event.Invoke;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.rtmp.status.Status;
import com.sms.server.net.rtmp.status.StatusCodes;
import com.sms.server.stream.codec.StreamCodecInfo;
import com.sms.server.stream.consumer.FileConsumer;
import com.sms.server.stream.message.RTMPMessage;
import com.sms.server.stream.message.StatusMessage;
import com.sms.server.util.SystemTimer;

/**
 * Represents live stream broadcasted from client. As Flash Media Server, Red5 supports
 * recording mode for live streams, that is, broadcasted stream has broadcast mode. It can be either
 * "live" or "record" and latter causes server-side application to record broadcasted stream.
 *
 * Note that recorded streams are recorded as FLV files. The same is correct for audio, because
 * NellyMoser codec that Flash Player uses prohibits on-the-fly transcoding to audio formats like MP3
 * without paying of licensing fee or buying SDK.
 *
 * This type of stream uses two different pipes for live streaming and recording.
 */
public class ClientBroadcastStream extends AbstractClientStream implements IClientBroadcastStream, IFilter, IPushableConsumer, IPipeConnectionListener, IEventDispatcher,
		IClientBroadcastStreamStatistics, ClientBroadcastStreamMXBean {

	private static final Logger log = LoggerFactory.getLogger(ClientBroadcastStream.class);

	/**
	 * Total number of bytes received.
	 */
	protected long bytesReceived;

	/**
	 * Is there need to check video codec?
	 */
	protected boolean checkVideoCodec = false;

	/**
	 * Is there need to check audio codec?
	 */
	protected boolean checkAudioCodec = false;

	/**
	 * Data is sent by chunks, each of them has size
	 */
	protected int chunkSize;

	/**
	 * Is this stream still active?
	 */
	protected volatile boolean closed;

	/**
	 * Output endpoint that providers use
	 */
	protected IMessageOutput connMsgOut;

	/** Stores timestamp of first packet. */
	protected long firstPacketTime = -1;

	/**
	 * Pipe for live streaming
	 */
	protected IPipe livePipe;

	/**
	 * MBean object name used for de/registration purposes.
	 */
	//private ObjectName oName;

	/**
	 * Stream published name
	 */
	protected String publishedName;

	/**
	 * Whether we are recording or not
	 */
	private volatile boolean recording;

	/**
	 * FileConsumer used to output recording to disk
	 */
	private FileConsumer recordingFile;

	/**
	 * The filename we are recording to.
	 */
	private String recordingFilename;

	/**
	 * Pipe for recording
	 */
	private IPipe recordPipe;

	/**
	 * Is there need to send start notification?
	 */
	protected boolean sendStartNotification = true;

	/**
	 * Stores statistics about subscribers.
	 */
	private StatisticsCounter subscriberStats = new StatisticsCounter();

	/** Listeners to get notified about received packets. */
	protected Set<IStreamListener> listeners = new CopyOnWriteArraySet<IStreamListener>();

	protected long latestTimeStamp = -1;
	
	private ObjectName oName;

	/**
	 * Check and send notification if necessary
	 * @param event          Event
	 */
	private void checkSendNotifications(IEvent event) {
		IEventListener source = event.getSource();
		sendStartNotifications(source);
	}

	/**
	 * Closes stream, unsubscribes provides, sends stoppage notifications and broadcast close notification.
	 */
	public void close() {
		log.info("Close");
		if (closed) {
			// Already closed
			return;
		}
		closed = true;
		if (livePipe != null) {
			livePipe.unsubscribe((IProvider) this);
		}
		if (recordPipe != null) {
			recordPipe.unsubscribe((IProvider) this);
			((AbstractPipe) recordPipe).close();
			recordPipe = null;
		}
		if (recording) {
			sendRecordStopNotify();
		}
		sendPublishStopNotify();
		// TODO: can we sent the client something to make sure he stops sending data?
		connMsgOut.unsubscribe(this);
		notifyBroadcastClose();
		// deregister with jmx
		JMXAgent.unregisterMBean(oName);
	}

	/**
	 * Dispatches event
	 * @param event          Event to dispatch
	 */
	public void dispatchEvent(IEvent event) {
		if (!(event instanceof IRTMPEvent) && (event.getType() != IEvent.Type.STREAM_CONTROL) && (event.getType() != IEvent.Type.STREAM_DATA) || closed) {
			// ignored event
			log.debug("dispatchEvent: {}", event.getType());
			return;
		}
		// get stream codec
		IStreamCodecInfo codecInfo = getCodecInfo();
		StreamCodecInfo info = null;
		if (codecInfo instanceof StreamCodecInfo) {
			info = (StreamCodecInfo) codecInfo;
		}
		// create the event
		IRTMPEvent rtmpEvent;
		try {
			rtmpEvent = (IRTMPEvent) event;
		} catch (ClassCastException e) {
			log.error("Class cast exception in event dispatch", e);
			return;
		}
		long eventTime = -1;
		if (log.isTraceEnabled()) {
			// If this is first packet save its timestamp; expect it is
			// absolute? no matter: it's never used!
			if (firstPacketTime == -1) {
				firstPacketTime = rtmpEvent.getTimestamp();
				log.trace(String.format("CBS=@%08x: rtmpEvent=%s creation=%s firstPacketTime=%d", System.identityHashCode(this), rtmpEvent.getClass().getSimpleName(),
						creationTime, firstPacketTime));
			} else {
				log.trace(String.format("CBS=@%08x: rtmpEvent=%s creation=%s firstPacketTime=%d timestamp=%d", System.identityHashCode(this), rtmpEvent.getClass().getSimpleName(),
						creationTime, firstPacketTime, rtmpEvent.getTimestamp()));
			}

		}
		//get the buffer only once per call
		IoBuffer buf = null;
		if (rtmpEvent instanceof IStreamData && (buf = ((IStreamData<?>) rtmpEvent).getData()) != null) {
			bytesReceived += buf.limit();
		}
		if (rtmpEvent instanceof AudioData) {
			// SplitmediaLabs - begin AAC fix
			IAudioStreamCodec audioStreamCodec = null;
			if (checkAudioCodec) {
				audioStreamCodec = AudioCodecFactory.getAudioCodec(buf);
				if (info != null) {
					info.setAudioCodec(audioStreamCodec);
				}
				checkAudioCodec = false;
			} else if (codecInfo != null) {
				audioStreamCodec = codecInfo.getAudioCodec();
			}
			if (audioStreamCodec != null) {
				audioStreamCodec.addData(buf.asReadOnlyBuffer());
			}
			if (info != null) {
				info.setHasAudio(true);
			}
			eventTime = rtmpEvent.getTimestamp();
			log.trace("Audio: {}", eventTime);
		} else if (rtmpEvent instanceof VideoData) {
			IVideoStreamCodec videoStreamCodec = null;
			if (checkVideoCodec) {
				videoStreamCodec = VideoCodecFactory.getVideoCodec(buf);
				if (info != null) {
					info.setVideoCodec(videoStreamCodec);
				}
				checkVideoCodec = false;
			} else if (codecInfo != null) {
				videoStreamCodec = codecInfo.getVideoCodec();
			}
			if (videoStreamCodec != null) {
				videoStreamCodec.addData(buf.asReadOnlyBuffer());
			}
			if (info != null) {
				info.setHasVideo(true);
			}
			eventTime = rtmpEvent.getTimestamp();
			log.trace("Video: {}", eventTime);
		} else if (rtmpEvent instanceof Invoke) {			
			eventTime = rtmpEvent.getTimestamp();
			//do we want to return from here?
			//event / stream listeners will not be notified of invokes
			return;
		} else if (rtmpEvent instanceof Notify) {
			//TDJ: store METADATA
			Notify notifyEvent = (Notify) rtmpEvent;
			if (notifyEvent.getHeader() != null && notifyEvent.getHeader().getDataType() == Notify.TYPE_STREAM_METADATA) {
				try {
					metaData = notifyEvent.duplicate();
				} catch (Exception e) {
					log.warn("Metadata could not be duplicated for this stream", e);
				}
			}
			eventTime = rtmpEvent.getTimestamp();
		}
		// update last event time
		if (eventTime > latestTimeStamp) {
			latestTimeStamp = eventTime;
		}
		// notify event listeners
		checkSendNotifications(event); 
		// note this timestamp is set in event/body but not in the associated header
		try {
			// route to recording
			if (recording) {
				if (recordPipe != null) {
					int bufferLimit = buf.limit();
					if (bufferLimit > 0) {
						// make a copy for the record pipe
						buf.mark();
						byte[] buffer = new byte[bufferLimit];
						buf.get(buffer);
						buf.reset();
						// Create new RTMP message, initialize it and push through pipe
						RTMPMessage msg = null;
						if (rtmpEvent instanceof AudioData) {
							AudioData audio = new AudioData(IoBuffer.wrap(buffer));
							audio.setTimestamp(eventTime);
							msg = RTMPMessage.build(audio);
						} else if (rtmpEvent instanceof VideoData) {
							VideoData video = new VideoData(IoBuffer.wrap(buffer));
							video.setTimestamp(eventTime);
							msg = RTMPMessage.build(video);
						} else if (rtmpEvent instanceof Notify) {
							Notify not = new Notify(IoBuffer.wrap(buffer));
							not.setTimestamp(eventTime);
							msg = RTMPMessage.build(not);
						} else {
							log.info("Data was not of A/V type: {}", rtmpEvent.getType());
							msg = RTMPMessage.build(rtmpEvent, eventTime);
						}
						// push it down to the recorder
						recordPipe.pushMessage(msg);
					} else {
						log.debug("Stream data size was 0, recording pipe will not be notified");
					}
				} else {
					log.debug("Record pipe was null, message was not pushed");
				}
			} else {
				log.trace("Recording not active");
			}
			// route to live
			if (livePipe != null) {
				// create new RTMP message, initialize it and push through pipe
				RTMPMessage msg = RTMPMessage.build(rtmpEvent, eventTime);
				livePipe.pushMessage(msg);
			} else {
				log.debug("Live pipe was null, message was not pushed");
			}
		} catch (IOException err) {
			sendRecordFailedNotify(err.getMessage());
			stop();
		}
		// Notify listeners about received packet
		if (rtmpEvent instanceof IStreamPacket) {
			for (IStreamListener listener : getStreamListeners()) {
				try {
					listener.packetReceived(this, (IStreamPacket) rtmpEvent);
				} catch (Exception e) {
					log.error("Error while notifying listener {}", listener, e);
				}
			}
		}
	}

	/** {@inheritDoc} */
	public int getActiveSubscribers() {
		return subscriberStats.getCurrent();
	}

	/** {@inheritDoc} */
	public long getBytesReceived() {
		return bytesReceived;
	}

	/** {@inheritDoc} */
	public long getCurrentTimestamp() {
		return  latestTimeStamp;
	}

	/** {@inheritDoc} */
	public int getMaxSubscribers() {
		return subscriberStats.getMax();
	}

	/**
	 * Getter for provider
	 * @return            Provider
	 */
	public IProvider getProvider() {
		return this;
	}

	/**
	 * Setter for stream published name
	 * @param name       Name that used for publishing. Set at client side when begin to broadcast with NetStream#publish.
	 */
	public void setPublishedName(String name) {
		log.debug("setPublishedName: {}", name);
		//check to see if we are setting the name to the same string		
		if (!name.equals(publishedName)) {
			// update an attribute
			JMXAgent.updateMBeanAttribute(oName, "publishedName", name);
		} else {
			//create a new mbean for this instance with the new name
			oName = JMXFactory.createObjectName("type", "ClientBroadcastStream", "publishedName", name);
			JMXAgent.registerMBean(this, this.getClass().getName(), ClientBroadcastStreamMXBean.class, oName);
		}
		this.publishedName = name;
	}

	/**
	 * Getter for published name
	 * @return        Stream published name
	 */
	public String getPublishedName() {
		return publishedName;
	}

	/** {@inheritDoc} */
	public String getSaveFilename() {
		return recordingFilename;
	}

	/** {@inheritDoc} */
	public IClientBroadcastStreamStatistics getStatistics() {
		return this;
	}

	/** {@inheritDoc} */
	public int getTotalSubscribers() {
		return subscriberStats.getTotal();
	}

	/**
	 *  Notifies handler on stream broadcast stop
	 */
	private void notifyBroadcastClose() {
		IStreamAwareScopeHandler handler = getStreamAwareHandler();
		if (handler != null) {
			try {
				handler.streamBroadcastClose(this);
			} catch (Throwable t) {
				log.error("Error in notifyBroadcastClose", t);
			}
		}
	}

	/**
	 *  Notifies handler on stream broadcast start
	 */
	private void notifyBroadcastStart() {
		IStreamAwareScopeHandler handler = getStreamAwareHandler();
		if (handler != null) {
			try {
				handler.streamBroadcastStart(this);
			} catch (Throwable t) {
				log.error("Error in notifyBroadcastStart", t);
			}
		}
	}

	/**
	 * Send OOB control message with chunk size
	 */
	private void notifyChunkSize() {
		if (chunkSize > 0 && livePipe != null) {
			OOBControlMessage setChunkSize = new OOBControlMessage();
			setChunkSize.setTarget("ConnectionConsumer");
			setChunkSize.setServiceName("chunkSize");
			if (setChunkSize.getServiceParamMap() == null) {
				setChunkSize.setServiceParamMap(new HashMap<String, Object>());
			}
			setChunkSize.getServiceParamMap().put("chunkSize", chunkSize);
			livePipe.sendOOBControlMessage(getProvider(), setChunkSize);
		}
	}

	/**
	 * Out-of-band control message handler
	 *
	 * @param source           OOB message source
	 * @param pipe             Pipe that used to send OOB message
	 * @param oobCtrlMsg       Out-of-band control message
	 */
	public void onOOBControlMessage(IMessageComponent source, IPipe pipe, OOBControlMessage oobCtrlMsg) {
		if ("ClientBroadcastStream".equals(oobCtrlMsg.getTarget())) {
			if ("chunkSize".equals(oobCtrlMsg.getServiceName())) {
				chunkSize = (Integer) oobCtrlMsg.getServiceParamMap().get("chunkSize");
				notifyChunkSize();
			}
		}
	}

	/**
	 * Pipe connection event handler
	 * @param event          Pipe connection event
	 */
	@SuppressWarnings("unused")
	public void onPipeConnectionEvent(PipeConnectionEvent event) {
		switch (event.getType()) {
			case PipeConnectionEvent.PROVIDER_CONNECT_PUSH:
				log.info("Provider connect");
				if (event.getProvider() == this && event.getSource() != connMsgOut && (event.getParamMap() == null || !event.getParamMap().containsKey("record"))) {

					this.livePipe = (IPipe) event.getSource();
					log.debug("Provider: {}", this.livePipe.getClass().getName());
					for (IConsumer consumer : this.livePipe.getConsumers()) {
						subscriberStats.increment();
					}
				}
				break;
			case PipeConnectionEvent.PROVIDER_DISCONNECT:
				log.info("Provider disconnect");
				if (log.isDebugEnabled() && this.livePipe != null) {
					log.debug("Provider: {}", this.livePipe.getClass().getName());
				}
				if (this.livePipe == event.getSource()) {
					this.livePipe = null;
				}
				break;
			case PipeConnectionEvent.CONSUMER_CONNECT_PUSH:
				log.info("Consumer connect");
				if(event.getSource() instanceof IPipe){
				IPipe pipe = (IPipe) event.getSource();
				if (log.isDebugEnabled() && pipe != null) {
					log.debug("Consumer: {}", pipe.getClass().getName());
				}
				if (this.livePipe == pipe) {
					notifyChunkSize();
				}
				}
				subscriberStats.increment();
				break;
			case PipeConnectionEvent.CONSUMER_DISCONNECT:
				log.info("Consumer disconnect");
				log.debug("Consumer: {}", event.getSource().getClass().getName());
				subscriberStats.decrement();
				break;
			default:
		}
	}

	/**
	 * Currently not implemented
	 *
	 * @param pipe           Pipe
	 * @param message        Message
	 */
	public void pushMessage(IPipe pipe, IMessage message) {
	}

	/**
	 * Save broadcasted stream.
	 *
	 * @param name                           Stream name
	 * @param isAppend                       Append mode
	 * @throws IOException					 File could not be created/written to.
	 * @throws ResourceNotFoundException     Resource doesn't exist when trying to append.
	 * @throws ResourceExistException        Resource exist when trying to create.
	 */
	public void saveAs(String name, boolean isAppend) throws IOException, ResourceNotFoundException, ResourceExistException {
		log.debug("SaveAs - name: {} append: {}", name, isAppend);

		Map<String, Object> recordParamMap = new HashMap<String, Object>(1);

		//setup record objects
		if (recordPipe == null) {
			recordPipe = new InMemoryPushPushPipe();
			// Clear record flag
			recordParamMap.put("record", null);
			recordPipe.subscribe((IProvider) this, recordParamMap);
			recordParamMap.clear();
		}

		// Get stream scope
		IStreamCapableConnection conn = getConnection();
		if (conn == null) {
			// TODO: throw other exception here?
			throw new IOException("Stream is no longer connected");
		}
		IScope scope = conn.getScope();
		// Get stream filename generator
		IStreamFilenameGenerator generator = (IStreamFilenameGenerator) ScopeUtils.getScopeService(scope, IStreamFilenameGenerator.class, DefaultStreamFilenameGenerator.class);

		// Generate filename
		recordingFilename = generator.generateFilename(scope, name, ".flv", GenerationType.RECORD);
		// Get file for that filename
		File file = null;
		if (generator.resolvesToAbsolutePath()) {
			file = new File(recordingFilename);
		} else {
			file = scope.getContext().getResource(recordingFilename);
		}
		//
		log.debug("File exists: {} writable: {}", file.exists(), file.canWrite());
		// If append mode is on...
		if (!isAppend) {
			if (file.exists()) {
				// Per livedoc of FCS/FMS:
				// When "live" or "record" is used,
				// any previously recorded stream with the same stream URI is deleted.
				if (!file.delete()) {
					throw new IOException(String.format("File: %s could not be deleted", file.getName()));
				}
			}
		} else {
			if (!file.exists()) {
				// Per livedoc of FCS/FMS:
				// If a recorded stream at the same URI does not already exist,
				// "append" creates the stream as though "record" was passed.
				isAppend = false;
			}
		}
		// if the file doesn't exist yet, create it
		if (!file.exists()) {
			// Make sure the destination directory exists
			String path = file.getAbsolutePath();
			int slashPos = path.lastIndexOf(File.separator);
			if (slashPos != -1) {
				path = path.substring(0, slashPos);
			}
			File tmp = new File(path);
			if (!tmp.isDirectory()) {
				tmp.mkdirs();
			}
			file.createNewFile();
		}
		//remove existing meta file
		File meta = new File(file.getCanonicalPath() + ".meta");
		if (meta.exists()) {
			log.trace("Meta file exists");
			if (meta.delete()) {
				log.debug("Meta file deleted - {}", meta.getName());
			} else {
				log.warn("Meta file was not deleted - {}", meta.getName());
				meta.deleteOnExit();
			}
		} else {
			log.debug("Meta file does not exist: {}", meta.getCanonicalPath());
		}
		log.debug("Recording file: {}", file.getCanonicalPath());
		// get instance via spring
//		if (scope.getContext().hasBean("fileConsumer")) {
			log.debug("Context contains a file consumer");
			recordingFile = (FileConsumer) scope.getContext().getBean(ScopeContextBean.FILECONSUMER_BEAN);
			recordingFile.setScope(scope);
			recordingFile.setFile(file);
//		} else {
//			log.debug("Context does not contain a file consumer, using direct instance");
//			// get a new instance
//			recordingFile = new FileConsumer(scope, file);
//		}
		//get decoder info if it exists for the stream
		IStreamCodecInfo codecInfo = getCodecInfo();
		log.debug("Codec info: {}", codecInfo);
		if (codecInfo instanceof StreamCodecInfo) {
			StreamCodecInfo info = (StreamCodecInfo) codecInfo;
			IVideoStreamCodec videoCodec = info.getVideoCodec();
			log.debug("Video codec: {}", videoCodec);
			if (videoCodec != null) {
				//check for decoder configuration to send
				IoBuffer config = videoCodec.getDecoderConfiguration();
				if (config != null) {
					log.debug("Decoder configuration is available for {}", videoCodec.getName());
					VideoData conf = new VideoData(config.asReadOnlyBuffer());
					try {
						log.debug("Setting decoder configuration for recording");
						recordingFile.setVideoDecoderConfiguration(conf);
					} finally {
						conf.release();
					}
				}
			} else {
				log.debug("Could not initialize stream output, videoCodec is null.");
			}
			// SplitmediaLabs - begin AAC fix
			IAudioStreamCodec audioCodec = info.getAudioCodec();
			log.debug("Audio codec: {}", audioCodec);
			if (audioCodec != null) {
				//check for decoder configuration to send
				IoBuffer config = audioCodec.getDecoderConfiguration();
				if (config != null) {
					log.debug("Decoder configuration is available for {}", audioCodec.getName());
					AudioData conf = new AudioData(config.asReadOnlyBuffer());
					try {
						log.debug("Setting decoder configuration for recording");
						recordingFile.setAudioDecoderConfiguration(conf);
					} finally {
						conf.release();
					}
				}
			} else {
				log.debug("No decoder configuration available, audioCodec is null.");
			}
		}
		if (isAppend) {
			recordParamMap.put("mode", "append");
		} else {
			recordParamMap.put("mode", "record");
		}
		//mark as "recording" only if we get subscribed
		recording = recordPipe.subscribe(recordingFile, recordParamMap);
	}

	/**
	 * Sends publish start notifications
	 */
	private void sendPublishStartNotify() {
		Status publishStatus = new Status(StatusCodes.NS_PUBLISH_START);
		publishStatus.setClientid(getStreamId());
		publishStatus.setDetails(getPublishedName());

		StatusMessage startMsg = new StatusMessage();
		startMsg.setBody(publishStatus);
		try {
			connMsgOut.pushMessage(startMsg);
		} catch (IOException err) {
			log.error("Error while pushing message.", err);
		}
	}

	/**
	 *  Sends publish stop notifications
	 */
	private void sendPublishStopNotify() {
		Status stopStatus = new Status(StatusCodes.NS_UNPUBLISHED_SUCCESS);
		stopStatus.setClientid(getStreamId());
		stopStatus.setDetails(getPublishedName());

		StatusMessage stopMsg = new StatusMessage();
		stopMsg.setBody(stopStatus);
		try {
			connMsgOut.pushMessage(stopMsg);
		} catch (IOException err) {
			log.error("Error while pushing message.", err);
		}
	}

	/**
	 *  Sends record failed notifications
	 */
	private void sendRecordFailedNotify(String reason) {
		Status failedStatus = new Status(StatusCodes.NS_RECORD_FAILED);
		failedStatus.setLevel(Status.ERROR);
		failedStatus.setClientid(getStreamId());
		failedStatus.setDetails(getPublishedName());
		failedStatus.setDesciption(reason);

		StatusMessage failedMsg = new StatusMessage();
		failedMsg.setBody(failedStatus);
		try {
			connMsgOut.pushMessage(failedMsg);
		} catch (IOException err) {
			log.error("Error while pushing message.", err);
		}
	}

	/**
	 *  Sends record start notifications
	 */
	private void sendRecordStartNotify() {
		Status recordStatus = new Status(StatusCodes.NS_RECORD_START);
		recordStatus.setClientid(getStreamId());
		recordStatus.setDetails(getPublishedName());

		StatusMessage startMsg = new StatusMessage();
		startMsg.setBody(recordStatus);
		try {
			connMsgOut.pushMessage(startMsg);
		} catch (IOException err) {
			log.error("Error while pushing message.", err);
		}
	}

	/**
	 *  Sends record stop notifications
	 */
	private void sendRecordStopNotify() {
		Status stopStatus = new Status(StatusCodes.NS_RECORD_STOP);
		stopStatus.setClientid(getStreamId());
		stopStatus.setDetails(getPublishedName());

		StatusMessage stopMsg = new StatusMessage();
		stopMsg.setBody(stopStatus);
		try {
			connMsgOut.pushMessage(stopMsg);
		} catch (IOException err) {
			log.error("Error while pushing message.", err);
		}
	}

	private void sendStartNotifications(IEventListener source) {
		if (sendStartNotification) {
			// Notify handler that stream starts recording/publishing
			sendStartNotification = false;
			if (source instanceof IConnection) {
				IScope scope = ((IConnection) source).getScope();
				if (scope.hasHandler()) {
					Object handler = scope.getHandler();
					if (handler instanceof IStreamAwareScopeHandler) {
						if (recording) {
							((IStreamAwareScopeHandler) handler).streamRecordStart(this);
						} else {
							((IStreamAwareScopeHandler) handler).streamPublishStart(this);
						}
					}
				}
			}
			// Send start notifications
			sendPublishStartNotify();
			if (recording) {
				sendRecordStartNotify();
			}
			notifyBroadcastStart();
		}
	}

	/**
	 * Starts stream. Creates pipes, connects
	 */
	public void start() {
		log.info("Stream start");
		IConsumerService consumerManager = (IConsumerService) getScope().getContext().getService(ScopeContextBean.CONSUMERSERVICE_BEAN);
		checkVideoCodec = true;
		checkAudioCodec = true;
		firstPacketTime = -1;
		latestTimeStamp = -1;
		connMsgOut = consumerManager.getConsumerOutput(this);
		connMsgOut.subscribe(this, null);
		setCodecInfo(new StreamCodecInfo());
		closed = false;
		bytesReceived = 0;
		creationTime = SystemTimer.currentTimeMillis();
		
		oName = JMXFactory.createObjectName("type", "ClientBroadcastStream", "publishedName", publishedName);
		JMXAgent.registerMBean(this, this.getClass().getName(), ClientBroadcastStreamMXBean.class, oName);
	}

	/** {@inheritDoc} */
	public void startPublishing() {
		// We send the start messages before the first packet is received.
		// This is required so FME actually starts publishing.
		sendStartNotifications(SMS.getConnectionLocal());
	}

	/** {@inheritDoc} */
	public void stop() {
		stopRecording();
		close();
	}

	/**
	 * Stops any currently active recordings.
	 */
	public void stopRecording() {
		if (recording) {
			recording = false;
			recordingFilename = null;
			recordPipe.unsubscribe(recordingFile);
			sendRecordStopNotify();
			recordPipe = null;
		}
	}

	public boolean isRecording() {
		return recording;
	}

	/** {@inheritDoc} */
	public void addStreamListener(IStreamListener listener) {
		listeners.add(listener);
	}

	/** {@inheritDoc} */
	public Collection<IStreamListener> getStreamListeners() {
		return listeners;
	}

	/** {@inheritDoc} */
	public void removeStreamListener(IStreamListener listener) {
		listeners.remove(listener);
	}

}
