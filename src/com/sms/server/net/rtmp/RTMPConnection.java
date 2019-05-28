package com.sms.server.net.rtmp;

import java.beans.ConstructorProperties;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.BaseConnection;
import com.sms.server.ScopeContextBean;
import com.sms.server.api.IScope;
import com.sms.server.api.SMS;
import com.sms.server.api.ScopeUtils;
import com.sms.server.api.event.IEvent;
import com.sms.server.api.scheduling.ISchedulingService;
import com.sms.server.api.service.IPendingServiceCall;
import com.sms.server.api.service.IPendingServiceCallback;
import com.sms.server.api.service.IServiceCall;
import com.sms.server.api.service.IServiceCapableConnection;
import com.sms.server.api.stream.IClientBroadcastStream;
import com.sms.server.api.stream.IClientStream;
import com.sms.server.api.stream.IPlaylistSubscriberStream;
import com.sms.server.api.stream.ISingleItemSubscriberStream;
import com.sms.server.api.stream.IStreamCapableConnection;
import com.sms.server.api.stream.IStreamService;
import com.sms.server.exception.ClientRejectedException;
import com.sms.server.net.rtmp.codec.RTMP;
import com.sms.server.net.rtmp.event.BytesRead;
import com.sms.server.net.rtmp.event.ClientBW;
import com.sms.server.net.rtmp.event.ClientInvokeEvent;
import com.sms.server.net.rtmp.event.ClientNotifyEvent;
import com.sms.server.net.rtmp.event.Invoke;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.event.Ping;
import com.sms.server.net.rtmp.event.ServerBW;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.net.rtmp.message.Packet;
import com.sms.server.service.Call;
import com.sms.server.service.PendingCall;
import com.sms.server.so.FlexSharedObjectMessage;
import com.sms.server.so.ISharedObjectEvent;
import com.sms.server.so.SharedObjectMessage;
import com.sms.server.stream.ClientBroadcastStream;
import com.sms.server.stream.OutputStream;
import com.sms.server.stream.PlaylistSubscriberStream;
import com.sms.server.stream.SingleItemSubscriberStream;
import com.sms.server.util.CustomizableThreadFactory;
import com.sms.server.util.SystemTimer;
import com.sms.server.util.timer.HashedWheelTimer;
import com.sms.server.util.timer.Timeout;
import com.sms.server.util.timer.Timer;
import com.sms.server.util.timer.TimerTask;

/**
 * @ClassName: RTMPConnection
 * @author pengliren
 *
 */
public abstract class RTMPConnection extends BaseConnection implements IStreamCapableConnection, IServiceCapableConnection {

	private static Logger log = LoggerFactory.getLogger(RTMPConnection.class);

	public static final String RTMP_CONNECTION_KEY = "rtmp.conn";

	public static final String RTMP_HANDSHAKE = "rtmp.handshake";

	/**
	 * Marker byte for standard or non-encrypted RTMP data.
	 */
	public static final byte RTMP_NON_ENCRYPTED = (byte) 0x03;

	/**
	 * Marker byte for encrypted RTMP data.
	 */
	public static final byte RTMP_ENCRYPTED = (byte) 0x06;

	/**
	 * Cipher for RTMPE input
	 */
	public static final String RTMPE_CIPHER_IN = "rtmpe.cipher.in";

	/**
	 * Cipher for RTMPE output
	 */
	public static final String RTMPE_CIPHER_OUT = "rtmpe.cipher.out";

	/**
	 * Connection channels
	 * 
	 * @see org.red5.server.net.rtmp.Channel
	 */
	private ConcurrentMap<Integer, Channel> channels = new ConcurrentHashMap<Integer, Channel>();

	/**
	 * Client streams
	 * 
	 * @see org.red5.server.api.stream.IClientStream
	 */
	private ConcurrentMap<Integer, IClientStream> streams = new ConcurrentHashMap<Integer, IClientStream>();

	private final BitSet reservedStreams = new BitSet();

	/**
	 * Identifier for remote calls.
	 */
	private AtomicInteger invokeId = new AtomicInteger(1);

	/**
	 * Hash map that stores pending calls and ids as pairs.
	 */
	private ConcurrentMap<Integer, IPendingServiceCall> pendingCalls = new ConcurrentHashMap<Integer, IPendingServiceCall>();

	/**
	 * Deferred results set.
	 * 
	 * @see org.red5.server.net.rtmp.DeferredResult
	 */
	private HashSet<DeferredResult> deferredResults = new HashSet<DeferredResult>();

	/**
	 * Last ping round trip time
	 */
	private AtomicInteger lastPingTime = new AtomicInteger(-1);

	/**
	 * Timestamp when last ping command was sent.
	 */
	private AtomicLong lastPingSent = new AtomicLong(0);

	/**
	 * Timestamp when last ping result was received.
	 */
	private AtomicLong lastPongReceived = new AtomicLong(0);

	/**
	 * Name of timer task that keeps connection alive.
	 */
	private Timeout keepAliveTimerout;
	
	private TimerTask keepAliveTimerTask;

	/**
	 * Ping interval in ms to detect dead clients.
	 */
	private volatile int pingInterval = 5000;

	/**
	 * Maximum time in ms after which a client is disconnected because of inactivity.
	 */
	private volatile int maxInactivity = 60000;

	/**
	 * Data read interval
	 */
	protected long bytesReadInterval = 1024 * 1024;

	/**
	 * Number of bytes to read next.
	 */
	protected long nextBytesRead = 1024 * 1024;

	/**
	 * Number of bytes the client reported to have received.
	 */
	private long clientBytesRead;

	/**
	 * Map for pending video packets and stream IDs.
	 */
	private ConcurrentMap<Integer, AtomicInteger> pendingVideos = new ConcurrentHashMap<Integer, AtomicInteger>();

	/**
	 * Number of streams used.
	 */
	private AtomicInteger usedStreams = new AtomicInteger(0);

	/**
	 * AMF version, AMF0 by default.
	 */
	private volatile Encoding encoding = Encoding.AMF0;

	/**
	 * Remembered stream buffer durations.
	 */
	private ConcurrentMap<Integer, Integer> streamBuffers = new ConcurrentHashMap<Integer, Integer>();

	/**
	 * Name of that is waiting for a valid handshake.
	 */
	private Timeout waitForHandshakeTimeout;

	/**
	 * Maximum time in milliseconds to wait for a valid handshake.
	 */
	private volatile int maxHandshakeTimeout = 5000;
	
	/**
	 * Bandwidth limit type / enforcement. (0=hard,1=soft,2=dynamic)
	 */
	protected int limitType = 0;

	protected volatile int clientId;

	/**
	 * protocol state
	 */
	protected volatile RTMP state;

	//private ISchedulingService schedulingService;
	
	private static Timer timer = new HashedWheelTimer(new CustomizableThreadFactory("RtmpConnTimerExecutor-"), 1, TimeUnit.SECONDS);
	
	/**
	 * Closing flag
	 */
	private final AtomicBoolean closing = new AtomicBoolean(false);	

	/**
	 * Creates anonymous RTMP connection without scope.
	 * 
	 * @param type Connection type
	 */
	@ConstructorProperties({ "type" })
	public RTMPConnection(String type) {
		// We start with an anonymous connection without a scope.
		// These parameters will be set during the call of "connect" later.
		super(type);
	}

	public int getId() {
		return clientId;
	}

	public void setId(int clientId) {
		this.clientId = clientId;
	}

	public RTMP getState() {
		return state;
	}

	public byte getStateCode() {
		return state.getState();
	}

	public void setStateCode(byte code) {
		state.setState(code);
	}

	public void setState(RTMP state) {
		log.debug("Set state: {}", state);
		this.state = state;
	}
	
	/** {@inheritDoc} */
	public void setBandwidth(int mbits) {
		// tell the flash player how fast we want data and how fast we shall send it
		getChannel(2).write(new ServerBW(mbits));
		// second param is the limit type (0=hard,1=soft,2=dynamic)
		getChannel(2).write(new ClientBW(mbits, (byte) limitType));
	}

	@Override
	public boolean connect(IScope newScope, Object[] params) {
		log.debug("Connect scope: {}", newScope);
		try {
			boolean success = super.connect(newScope, params);
			if (success) {
				unscheduleWaitForHandshakeJob();
			}
			return success;
		} catch (ClientRejectedException e) {
			log.warn("Client rejected, unscheduling waitForHandshakeJob", e);
			unscheduleWaitForHandshakeJob();
			throw e;
		}
	}

	private void unscheduleWaitForHandshakeJob() {
		getWriteLock().lock();
		try {
			if (waitForHandshakeTimeout != null) {
				waitForHandshakeTimeout.cancel();
				waitForHandshakeTimeout = null;
				log.debug("Removed waitForHandshakeJob for: {}", getId());
			}
		} finally {
			getWriteLock().unlock();
		}
	}

	/**
	 * Initialize connection.
	 * 
	 * @param host Connection host
	 * @param path Connection path
	 * @param sessionId Connection session id
	 * @param params Params passed from client
	 */
	public void setup(String host, String path, String sessionId, Map<String, Object> params) {
		this.host = host;
		this.path = path;
		this.sessionId = sessionId;
		this.params = params;
		if (params.get("objectEncoding") == Integer.valueOf(3)) {
			log.info("Setting object encoding to AMF3");
			encoding = Encoding.AMF3;
		}
	}

	/**
	 * Return AMF protocol encoding used by this connection.
	 * 
	 * @return AMF encoding used by connection
	 */
	public Encoding getEncoding() {
		return encoding;
	}

	/**
	 * Getter for next available channel id.
	 * 
	 * @return Next available channel id
	 */
	public int getNextAvailableChannelId() {
		int result = 4;
		while (isChannelUsed(result)) {
			result++;
		}
		return result;
	}

	/**
	 * Checks whether channel is used.
	 * 
	 * @param channelId Channel id
	 * @return <code>true</code> if channel is in use, <code>false</code>
	 *         otherwise
	 */
	public boolean isChannelUsed(int channelId) {
		return channels.get(channelId) != null;
	}

	/**
	 * Return channel by id.
	 * 
	 * @param channelId Channel id
	 * @return Channel by id
	 */
	public Channel getChannel(int channelId) {
		
		 if (channels == null) {
	         return new Channel(null, channelId);
	     }
		 
		final Channel value = new Channel(this, channelId);
		Channel result = channels.putIfAbsent(channelId, value);
		if (result == null) {
			result = value;
		}
		return result;
	}

	/**
	 * Closes channel.
	 * 
	 * @param channelId Channel id
	 */
	public void closeChannel(int channelId) {
		channels.remove(channelId);
	}

	/**
	 * Getter for client streams.
	 * 
	 * @return Client streams as array
	 */
	protected Collection<IClientStream> getStreams() {
		return streams.values();
	}

	/** {@inheritDoc} */
	public int reserveStreamId() {
		int result = -1;
		getWriteLock().lock();
		try {
			for (int i = 0; true; i++) {
				if (!reservedStreams.get(i)) {
					reservedStreams.set(i);
					result = i;
					break;
				}
			}
		} finally {
			getWriteLock().unlock();
		}
		return result + 1;
	}
	
	/** {@inheritDoc} */
	public int reserveStreamId(int id) {
		int result = -1;
		getWriteLock().lock();
		try{
			if(!reservedStreams.get(id-1)){
				reservedStreams.set(id-1);
				result = id-1;
			}else{
				result = reserveStreamId();
			}
		}finally{
			getWriteLock().unlock();
		}
		return result;
	}
	
	/**
	 * Returns whether or not a given stream id is valid.
	 * 
	 * @param streamId
	 * @return true if its valid, false if its invalid
	 */
	public boolean isValidStreamId(int streamId) {
		
		getReadLock().lock();
		try {
			int index = streamId - 1;
			if (index < 0 || !reservedStreams.get(index)) {
				// stream id has not been reserved before
				return false;
			}
			if (streams.get(streamId - 1) != null) {
				// another stream already exists with this id
				return false;
			}
			return true;
		} finally {
			getReadLock().unlock();
		}
	}
	
	/**
	 * Returns whether or not the connection has been idle for a maximum period.
	 * 
	 * @return true if max idle period has been exceeded, false otherwise
	 */
	public boolean isIdle() {
		long lastPingTime = lastPingSent.get();
		long lastPongTime = lastPongReceived.get();
		boolean idle = (lastPongTime > 0 && (lastPingTime - lastPongTime > maxInactivity));
		log.trace("Connection {} idle", idle ? "is" : "is not");
		return idle;
	}

	/**
	 * Creates output stream object from stream id. Output stream consists of
	 * audio, data and video channels.
	 * 
	 * @see org.red5.server.stream.OutputStream
	 * 
	 * @param streamId Stream id
	 * @return Output stream object
	 */
	public OutputStream createOutputStream(int streamId) {
		int channelId = (4 + ((streamId - 1) * 5));
		//final Channel data = getChannel(channelId++);
		//final Channel video = getChannel(channelId++);
	//	final Channel audio = getChannel(channelId++);
		//fixed by kinov:to use signle stream for data audio and video for prevent from  useing absolute timestamp in rtmpencoder.
		final Channel data = getChannel(channelId);
		final Channel video = getChannel(channelId);
		final Channel audio = getChannel(channelId);
		// final Channel unknown = getChannel(channelId++);
		// final Channel ctrl = getChannel(channelId++);
		return new OutputStream(video, audio, data);
	}

	/** {@inheritDoc} */
	public IClientBroadcastStream newBroadcastStream(int streamId) {
		if (isValidStreamId(streamId)) {
			ClientBroadcastStream cbs = (ClientBroadcastStream) scope.getContext().getBean(ScopeContextBean.BROADCASTSTREAM_BEAN);
			Integer buffer = streamBuffers.get(streamId - 1);
			if (buffer != null) {
				cbs.setClientBufferDuration(buffer);
			}
			cbs.setStreamId(streamId);
			cbs.setConnection(this);
			cbs.setName(createStreamName());
			cbs.setScope(this.getScope());

			registerStream(cbs);
			usedStreams.incrementAndGet();
			return cbs;
		}

		return null;
	}

	/** {@inheritDoc} */
	public ISingleItemSubscriberStream newSingleItemSubscriberStream(int streamId) {
		
		if (isValidStreamId(streamId)) {
			SingleItemSubscriberStream siss = (SingleItemSubscriberStream) scope.getContext().getBean(ScopeContextBean.SINGLESTREAM_BEAN);
			Integer buffer = streamBuffers.get(streamId - 1);
			if (buffer != null) {
				siss.setClientBufferDuration(buffer);
			}
			siss.setName(createStreamName());
			siss.setConnection(this);
			siss.setScope(this.getScope());
			siss.setStreamId(streamId);
			registerStream(siss);
			usedStreams.incrementAndGet();
			return siss;
		}
		
		return null;
	}

	/** {@inheritDoc} */
	public IPlaylistSubscriberStream newPlaylistSubscriberStream(int streamId) {
		
		if (isValidStreamId(streamId)) {
			
			PlaylistSubscriberStream pss = (PlaylistSubscriberStream) scope.getContext().getBean(ScopeContextBean.SUBSCRIBERSTREAM_BEAN);
			Integer buffer = streamBuffers.get(streamId - 1);
			if (buffer != null) {
				pss.setClientBufferDuration(buffer);
			}
			pss.setName(createStreamName());
			pss.setConnection(this);
			pss.setScope(this.getScope());
			pss.setStreamId(streamId);
			registerStream(pss);
			usedStreams.incrementAndGet();
			return pss;
		}
		
		return null;
	}

	public void addClientStream(IClientStream stream) {
		int streamId = stream.getStreamId();
		getWriteLock().lock();
		try {
			if (reservedStreams.get(streamId - 1)) {
				return;
			}
			reservedStreams.set(streamId - 1);
		} finally {
			getWriteLock().unlock();
		}
		streams.put(streamId - 1, stream);
		usedStreams.incrementAndGet();
	}

	public void removeClientStream(int streamId) {
		unreserveStreamId(streamId);
	}

	/**
	 * Getter for used stream count.
	 * 
	 * @return Value for property 'usedStreamCount'.
	 */
	protected int getUsedStreamCount() {
		return usedStreams.get();
	}

	/** {@inheritDoc} */
	public IClientStream getStreamById(int id) {
		if (id <= 0) {
			return null;
		}
		return streams.get(id - 1);
	}

	/**
	 * Return stream id for given channel id.
	 * 
	 * @param channelId Channel id
	 * @return ID of stream that channel belongs to
	 */
	public int getStreamIdForChannel(int channelId) {
		if (channelId < 4) {
			return 0;
		}
		return ((channelId - 4) / 5) + 1;
	}

	/**
	 * Return stream by given channel id.
	 * 
	 * @param channelId Channel id
	 * @return Stream that channel belongs to
	 */
	public IClientStream getStreamByChannelId(int channelId) {
		if (channelId < 4) {
			return null;
		}
		return streams.get(getStreamIdForChannel(channelId) - 1);
	}

	/**
	 * Store a stream in the connection.
	 * 
	 * @param stream
	 */
	private void registerStream(IClientStream stream) {
		streams.put(stream.getStreamId() - 1, stream);
	}

	/**
	 * Remove a stream from the connection.
	 * 
	 * @param stream
	 */
	@SuppressWarnings("unused")
	private void unregisterStream(IClientStream stream) {
		streams.remove(stream.getStreamId());
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		
		if (closing.compareAndSet(false, true)) {
			log.debug("close: {}", sessionId);
			// update our state
			if (state != null) {
				final byte s = getStateCode();
				switch (s) {
					case RTMP.STATE_DISCONNECTED:
						log.debug("Already disconnected");
						return;
					default:
						log.debug("State: {}", state.states[s]);
						state.setState(RTMP.STATE_DISCONNECTING);
				}
			}
			
			getWriteLock().lock();
			try {
				if (keepAliveTimerout != null) {
					keepAliveTimerout.cancel();
					keepAliveTimerout = null;
				}
			} finally {
				getWriteLock().unlock();
			}
			SMS.setConnectionLocal(this);
			IStreamService streamService = (IStreamService) ScopeUtils.getScopeService(scope, ScopeContextBean.STREAMSERVICE_BEAN);		
			if (streamService != null) {
				for (Map.Entry<Integer, IClientStream> entry : streams.entrySet()) {
					IClientStream stream = entry.getValue();
					if (stream != null) {
						log.debug("Closing stream: {}", stream.getStreamId());
						streamService.deleteStream(this, stream.getStreamId());
						usedStreams.decrementAndGet();
					}
				}
				streams.clear();
			}
			// close the base connection - disconnect scopes and unregister client
			super.close();
			// kill all the collections etc
			if (channels != null) {
				channels.clear();
				channels = null;
			} else {
				log.trace("Channels collection was null");
			}
			if (streams != null) {
				streams.clear();
				streams = null;
			} else {
				log.trace("Streams collection was null");
			}
			if (pendingCalls != null) {
				pendingCalls.clear();
				pendingCalls = null;
			} else {
				log.trace("PendingCalls collection was null");
			}
			if (deferredResults != null) {
				deferredResults.clear();
				deferredResults = null;
			} else {
				log.trace("DeferredResults collection was null");
			}
			if (pendingVideos != null) {
				pendingVideos.clear();
				pendingVideos = null;
			} else {
				log.trace("PendingVideos collection was null");
			}
			if (streamBuffers != null) {
				streamBuffers.clear();
				streamBuffers = null;
			} else {
				log.trace("StreamBuffers collection was null");
			}
			
			// clear thread local reference
			SMS.setConnectionLocal(null);
		} else {
			log.debug("Already closing..");
		}
	}
	
	/**
	 * Dispatches event
	 * @param event       Event
	 */
	@Override
	public void dispatchEvent(IEvent event) {
		log.debug("Event notify: {}", event);
		// determine if its an outgoing invoke or notify
		switch (event.getType()) {
			case CLIENT_INVOKE:
				ClientInvokeEvent cie = (ClientInvokeEvent) event;
				invoke(cie.getMethod(), cie.getParams(), cie.getCallback());
				break;
			case CLIENT_NOTIFY:
				ClientNotifyEvent cne = (ClientNotifyEvent) event;
				notify(cne.getMethod(), cne.getParams());
				break;
			default:
				log.warn("Unhandled event: {}", event);
		}
	}	

	/**
	 * When the connection has been closed, notify any remaining pending service calls that they have failed because
	 * the connection is broken. Implementors of IPendingServiceCallback may only deduce from this notification that
	 * it was not possible to read a result for this service call. It is possible that (1) the service call was never
	 * written to the service, or (2) the service call was written to the service and although the remote method was
	 * invoked, the connection failed before the result could be read, or (3) although the remote method was invoked
	 * on the service, the service implementor detected the failure of the connection and performed only partial
	 * processing. The caller only knows that it cannot be confirmed that the callee has invoked the service call
	 * and returned a result.
	 */
	public void sendPendingServiceCallsCloseError() {
		if (pendingCalls != null && !pendingCalls.isEmpty()) {
			for (IPendingServiceCall call : pendingCalls.values()) {
				call.setStatus(Call.STATUS_NOT_CONNECTED);
				for (IPendingServiceCallback callback : call.getCallbacks()) {
					callback.resultReceived(call);
				}
			}
		}
	}

	/** {@inheritDoc} */
	public void unreserveStreamId(int streamId) {
		getWriteLock().lock();
		try {
			deleteStreamById(streamId);
			if (streamId > 0) {
				reservedStreams.clear(streamId - 1);
			}
		} finally {
			getWriteLock().unlock();
		}
	}

	/** {@inheritDoc} */
	public void deleteStreamById(int streamId) {
		if (streamId > 0) {
			if (streams.get(streamId - 1) != null) {
				pendingVideos.remove(streamId);
				usedStreams.decrementAndGet();
				streams.remove(streamId - 1);
				streamBuffers.remove(streamId - 1);
			}
		}
	}

	/**
	 * Handler for ping event.
	 * 
	 * @param ping Ping event context
	 */
	public void ping(Ping ping) {
		getChannel(2).write(ping);
	}

	/**
	 * Write raw byte buffer.
	 * 
	 * @param out IoBuffer
	 */
	public abstract void rawWrite(IoBuffer out);

	/**
	 * Write packet.
	 * 
	 * @param out Packet
	 */
	public abstract void write(Packet out);

	/**
	 * Update number of bytes to read next value.
	 */
	protected void updateBytesRead() {
		getWriteLock().lock();
		try {
			long bytesRead = getReadBytes();
			if (bytesRead >= nextBytesRead) {
				BytesRead sbr = new BytesRead((int) (bytesRead % Integer.MAX_VALUE));
				getChannel(2).write(sbr);
				nextBytesRead += bytesReadInterval;
			}
		} finally {
			getWriteLock().unlock();
		}
	}

	/**
	 * Read number of received bytes.
	 * 
	 * @param bytes Number of bytes
	 */
	public void receivedBytesRead(int bytes) {
		getWriteLock().lock();
		try {
			log.debug("Client received {} bytes, written {} bytes, {} messages pending", new Object[] { bytes, getWrittenBytes(), getPendingMessages() });
			clientBytesRead = bytes;
		} finally {
			getWriteLock().unlock();
		}
	}

	/**
	 * Get number of bytes the client reported to have received.
	 * 
	 * @return Number of bytes
	 */
	public long getClientBytesRead() {
		getReadLock().lock();
		try {
			return clientBytesRead;
		} finally {
			getReadLock().unlock();
		}
	}

	/** {@inheritDoc} */
	public void invoke(IServiceCall call) {
		invoke(call, 3);
	}

	/**
	 * Generate next invoke id.
	 * 
	 * @return Next invoke id for RPC
	 */
	public int getInvokeId() {
		return invokeId.incrementAndGet();
	}

	/**
	 * Register pending call (remote function call that is yet to finish).
	 * 
	 * @param invokeId Deferred operation id
	 * @param call Call service
	 */
	public void registerPendingCall(int invokeId, IPendingServiceCall call) {
		pendingCalls.put(invokeId, call);
	}

	/** {@inheritDoc} */
	public void invoke(IServiceCall call, int channel) {
		// We need to use Invoke for all calls to the client
		Invoke invoke = new Invoke();
		invoke.setCall(call);
		invoke.setInvokeId(getInvokeId());
		if (call instanceof IPendingServiceCall) {
			registerPendingCall(invoke.getInvokeId(), (IPendingServiceCall) call);
		}
		getChannel(channel).write(invoke);
	}

	/** {@inheritDoc} */
	public void invoke(String method) {
		invoke(method, null, null);
	}

	/** {@inheritDoc} */
	public void invoke(String method, Object[] params) {
		invoke(method, params, null);
	}

	/** {@inheritDoc} */
	public void invoke(String method, IPendingServiceCallback callback) {
		invoke(method, null, callback);
	}

	/** {@inheritDoc} */
	public void invoke(String method, Object[] params, IPendingServiceCallback callback) {
		IPendingServiceCall call = new PendingCall(method, params);
		if (callback != null) {
			call.registerCallback(callback);
		}
		invoke(call);
	}

	/** {@inheritDoc} */
	public void notify(IServiceCall call) {
		notify(call, 3);
	}

	/** {@inheritDoc} */
	public void notify(IServiceCall call, int channel) {
		Notify notify = new Notify();
		notify.setCall(call);
		getChannel(channel).write(notify);
	}

	/** {@inheritDoc} */
	public void notify(String method) {
		notify(method, null);
	}

	/** {@inheritDoc} */
	public void notify(String method, Object[] params) {
		IServiceCall call = new Call(method, params);
		notify(call);
	}

	/** {@inheritDoc} */
	@Override
	public long getReadBytes() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public long getWrittenBytes() {
		return 0;
	}

	/**
	 * Get pending call service by id.
	 * 
	 * @param invokeId
	 *            Pending call service id
	 * @return Pending call service object
	 */
	protected IPendingServiceCall getPendingCall(int invokeId) {
		return pendingCalls.get(invokeId);
	}

	/**
	 * Retrieve pending call service by id. The call will be removed afterwards.
	 * 
	 * @param invokeId
	 *            Pending call service id
	 * @return Pending call service object
	 */
	protected IPendingServiceCall retrievePendingCall(int invokeId) {
		return pendingCalls.remove(invokeId);
	}

	/**
	 * Generates new stream name.
	 * 
	 * @return New stream name
	 */
	protected String createStreamName() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Mark message as being written.
	 * 
	 * @param message
	 *            Message to mark
	 */
	protected void writingMessage(Packet message) {
		if (message.getMessage() instanceof VideoData) {
			int streamId = message.getHeader().getStreamId();
			final AtomicInteger value = new AtomicInteger();
			AtomicInteger old = pendingVideos.putIfAbsent(streamId, value);
			if (old == null) {
				old = value;
			}
			old.incrementAndGet();
		}
	}

	/**
	 * Increases number of read messages by one. Updates number of bytes read.
	 */
	public void messageReceived() {
		readMessages.incrementAndGet();
		// Trigger generation of BytesRead messages
		updateBytesRead();
	}

	/**
	 * Mark message as sent.
	 * 
	 * @param message
	 *            Message to mark
	 */
	public void messageSent(Packet message) {
		if (message.getMessage() instanceof VideoData) {
			int streamId = message.getHeader().getStreamId();
			AtomicInteger pending = pendingVideos.get(streamId);
			if (pending != null) {
				pending.decrementAndGet();
			}
		}
		writtenMessages.incrementAndGet();
	}

	/**
	 * Increases number of dropped messages.
	 */
	protected void messageDropped() {
		droppedMessages.incrementAndGet();
	}

	/** {@inheritDoc} */
	@Override
	public long getPendingVideoMessages(int streamId) {
		AtomicInteger count = pendingVideos.get(streamId);
		long result = (count != null ? count.intValue() - getUsedStreamCount() : 0);
		return (result > 0 ? result : 0);
	}
	
	/**
	 * Send a shared object message.
	 * 
	 * @param name shared object name
	 * @param currentVersion the current version
	 * @param persistent 
	 * @param events
	 */
	public void sendSharedObjectMessage(String name, int currentVersion, boolean persistent, ConcurrentLinkedQueue<ISharedObjectEvent> events) {
		// get the channel for so updates
		Channel channel = getChannel((byte) 3);
		log.trace("Send to channel: {}", channel);
		// create a new sync message for every client to avoid concurrent access through multiple threads
		SharedObjectMessage syncMessage = encoding == Encoding.AMF3 ? new FlexSharedObjectMessage(null, name, currentVersion, persistent) : new SharedObjectMessage(null, name,
				currentVersion, persistent);
		syncMessage.addEvents(events);
		try {
			channel.write(syncMessage);
		} catch (Exception e) {
			log.warn("Exception sending shared object", e);
		}
	}

	/** {@inheritDoc} */
	public void ping() {
		long newPingTime = SystemTimer.currentTimeMillis();
		log.debug("Pinging client with id {} at {}, last ping sent at {}", new Object[] { getId(), newPingTime, lastPingSent.get() });
		if (lastPingSent.get() == 0) {
			lastPongReceived.set(newPingTime);
		}
		Ping pingRequest = new Ping();
		pingRequest.setEventType(Ping.PING_CLIENT);
		lastPingSent.set(newPingTime);
		int now = (int) (newPingTime & 0xffffffff);
		pingRequest.setValue2(now);
		ping(pingRequest);
	}

	/**
	 * Marks that ping back was received.
	 * 
	 * @param pong
	 *            Ping object
	 */
	public void pingReceived(Ping pong) {
		long now = SystemTimer.currentTimeMillis();
		long previousReceived = (int) (lastPingSent.get() & 0xffffffff);
		log.debug("Pong from client id {} at {} with value {}, previous received at {}", new Object[] { getId(), now, pong.getValue2(), previousReceived });
		if (pong.getValue2() == previousReceived) {
			lastPingTime.set((int) (now & 0xffffffff) - pong.getValue2());
		}
		lastPongReceived.set(now);
	}

	/** {@inheritDoc} */
	public int getLastPingTime() {
		return lastPingTime.get();
	}

	/**
	 * Setter for ping interval.
	 * 
	 * @param pingInterval Interval in ms to ping clients. Set to <code>0</code> to
	 *            disable ghost detection code.
	 */
	public void setPingInterval(int pingInterval) {
		this.pingInterval = pingInterval;
	}

	/**
	 * Setter for maximum inactivity.
	 * 
	 * @param maxInactivity Maximum time in ms after which a client is disconnected in
	 *            case of inactivity.
	 */
	public void setMaxInactivity(int maxInactivity) {
		this.maxInactivity = maxInactivity;
	}

	/**
	 * Starts measurement.
	 */
	public void startRoundTripMeasurement() {
		if (pingInterval > 0 && keepAliveTimerout == null) {
			keepAliveTimerTask = new KeepAliveTask();
			keepAliveTimerout = timer.newTimeout(keepAliveTimerTask, pingInterval, TimeUnit.MILLISECONDS);
			log.debug("Keep alive task for client id {}", getId());
		}
	}

	/**
	 * Inactive state event handler.
	 */
	protected abstract void onInactive();

	/** {@inheritDoc} */
	@Override
	public String toString() {
		Object[] args = new Object[] { getClass().getSimpleName(), getRemoteAddress(), getRemotePort(), getHost(), getReadBytes(), getWrittenBytes() };
		return String.format("%1$s from %2$s : %3$s to %4$s (in: %5$s out %6$s )", args);
	}

	/**
	 * Registers deferred result.
	 * 
	 * @param result Result to register
	 */
	protected void registerDeferredResult(DeferredResult result) {
		getWriteLock().lock();
		try {
			deferredResults.add(result);
		} finally {
			getWriteLock().unlock();
		}
	}

	/**
	 * Unregister deferred result
	 * 
	 * @param result
	 *            Result to unregister
	 */
	protected void unregisterDeferredResult(DeferredResult result) {
		getWriteLock().lock();
		try {
			deferredResults.remove(result);
		} finally {
			getWriteLock().unlock();
		}
	}

	protected void rememberStreamBufferDuration(int streamId, int bufferDuration) {
		streamBuffers.put(streamId - 1, bufferDuration);
	}

	/**
	 * Set maximum time to wait for valid handshake in milliseconds.
	 * 
	 * @param maxHandshakeTimeout Maximum time in milliseconds
	 */
	public void setMaxHandshakeTimeout(int maxHandshakeTimeout) {
		this.maxHandshakeTimeout = maxHandshakeTimeout;
	}

	/**
	 * Start waiting for a valid handshake.
	 * 
	 * @param service
	 *            The scheduling service to use
	 */
	protected void startWaitForHandshake(ISchedulingService service) {
		waitForHandshakeTimeout = timer.newTimeout(new WaitForHandshakeTask(), maxHandshakeTimeout, TimeUnit.MILLISECONDS);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + clientId;
		if (host != null) {
			result = result + host.hashCode();
		}
		if (remoteAddress != null) {
			result = result + remoteAddress.hashCode();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RTMPConnection other = (RTMPConnection) obj;
		if (clientId != other.clientId) {
			return false;
		}
		if (host != null && !host.equals(other.getHost())) {
			return false;
		}
		if (remoteAddress != null && !remoteAddress.equals(other.getRemoteAddress())) {
			return false;
		}
		return true;
	}

	/**
	 * Quartz job that keeps connection alive and disconnects if client is dead.
	 */
	private class KeepAliveTask implements TimerTask {

		private final AtomicBoolean running = new AtomicBoolean(false);
		
		private final AtomicLong lastBytesRead = new AtomicLong(0);

		private volatile long lastBytesReadTime = 0;

		@Override
		public void run(Timeout timeout) throws Exception {
			// TODO Auto-generated method stub
			// ensure the job is not already running
			if (running.compareAndSet(false, true)) {
				// first check connected
				if (isConnected()) {
					// get now
					long now = System.currentTimeMillis();
					// get the current bytes read count on the connection
					long currentReadBytes = getReadBytes();
					// get our last bytes read count
					long previousReadBytes = lastBytesRead.get();
					log.trace("Time now: {} current read count: {} last read count: {}", new Object[] { now, currentReadBytes, previousReadBytes });
					if (currentReadBytes > previousReadBytes) {
						log.trace("Client is still alive, no ping needed");
						// client has sent data since last check and thus is not dead. No need to ping
						if (lastBytesRead.compareAndSet(previousReadBytes, currentReadBytes)) {
							// update the timestamp to match our update
							lastBytesReadTime = now;
						}
						// check idle
						if (isIdle()) {
							onInactive();
						}
						// update last ping time to prevent overly active pinging
						lastPingSent.set(now);
					} else if (getPendingMessages() > 0) {
						// client may not have updated bytes yet, but may have received messages waiting, no need to drop them if processing hasn't
						// caught up yet
						log.trace("Reader is not idle, possible flood. Pending write messages: {}", getPendingMessages());
						// update last ping time to prevent overly active pinging
						lastPingSent.set(now);
					} else {
						// client didn't send response to ping command and didn't sent data for too long, disconnect
						long lastPingTime = lastPingSent.get();
						long lastPongTime = lastPongReceived.get();
						if (lastPongTime > 0 && (lastPingTime - lastPongTime > maxInactivity) && !(now - lastBytesReadTime < maxInactivity)) {
							log.warn("Closing {}, with id {}, due to too much inactivity ({} ms), last ping sent {} ms ago", new Object[] { RTMPConnection.this, getId(),
									(lastPingTime - lastPongTime), (now - lastPingTime) });
							// the following line deals with a very common support request
							log.warn("This often happens if YOUR Red5 application generated an exception on start-up. Check earlier in the log for that exception first!");
							onInactive();
						}
						// send ping command to client to trigger sending of data
						ping();
					}
					// reset running flag
					running.compareAndSet(true, false);
					keepAliveTimerout = timer.newTimeout(keepAliveTimerTask, pingInterval, TimeUnit.MILLISECONDS);
				} else {
					log.debug("No longer connected, clean up connection. Connection state: {}", state.states[state.getState()]);
					onInactive();
				}
			} else {
				keepAliveTimerout = timer.newTimeout(keepAliveTimerTask, pingInterval, TimeUnit.MILLISECONDS);
			}
		}
	}

	/**
	 * Quartz job that waits for a valid handshake and disconnects the client if
	 * none is received.
	 */
	private class WaitForHandshakeTask implements TimerTask {

		@Override
		public void run(Timeout timeout) throws Exception {
			waitForHandshakeTimeout = null;
			// Client didn't send a valid handshake, disconnect
			log.warn("Closing {}, with id {} due to long handshake", RTMPConnection.this, getId());
			onInactive();
		}
	}
}
