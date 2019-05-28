package com.sms.server.stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.utils.ObjectMap;
import com.sms.server.api.service.IPendingServiceCall;
import com.sms.server.api.service.IPendingServiceCallback;
import com.sms.server.messaging.IMessage;
import com.sms.server.messaging.IMessageComponent;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.IPipeConnectionListener;
import com.sms.server.messaging.IPushableConsumer;
import com.sms.server.messaging.OOBControlMessage;
import com.sms.server.messaging.PipeConnectionEvent;
import com.sms.server.net.rtmp.INetStreamEventHandler;
import com.sms.server.net.rtmp.RTMPClient;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.status.StatusCodes;
import com.sms.server.stream.message.RTMPMessage;

/**
 * A proxy to publish stream from server to server.
 *
 * TODO: Use timer to monitor the connect/stream creation.
 */
public class StreamingProxy implements IPushableConsumer, IPipeConnectionListener, INetStreamEventHandler,
		IPendingServiceCallback {

	private static Logger log = LoggerFactory.getLogger(StreamingProxy.class);

	private List<IMessage> frameBuffer = new ArrayList<IMessage>();

	public static final String LIVE = "live";

	public static final String RECORD = "record";

	public static final String APPEND = "append";

	private static final int STOPPED = 0;

	private static final int CONNECTING = 1;

	private static final int STREAM_CREATING = 2;

	private static final int PUBLISHING = 3;

	private static final int PUBLISHED = 4;

	private String host;

	private int port;

	private String app;

	private RTMPClient rtmpClient;

	private int state;

	private String publishName;

	private int streamId;

	private String publishMode;

	public void init() {
		rtmpClient = new RTMPClient();
		state = STOPPED;
	}

	public synchronized void start(String publishName, String publishMode, Object[] params) {
		state = CONNECTING;
		this.publishName = publishName;
		this.publishMode = publishMode;

		Map<String, Object> defParams = rtmpClient.makeDefaultConnectionParams(host, port, app);
		rtmpClient.connect(host, port, defParams, this, params);
	}

	public synchronized void stop() {
		if (state >= STREAM_CREATING) {
			rtmpClient.disconnect();
		}
		state = STOPPED;
	}

	public void onPipeConnectionEvent(PipeConnectionEvent event) {
		// nothing to do
	}

	synchronized public void pushMessage(IPipe pipe, IMessage message) throws IOException {
		if (state >= PUBLISHED && message instanceof RTMPMessage) {
			RTMPMessage rtmpMsg = (RTMPMessage) message;
			rtmpClient.publishStreamData(streamId, rtmpMsg);
		} else {
			frameBuffer.add(message);
		}
	}

	public void onOOBControlMessage(IMessageComponent source, IPipe pipe, OOBControlMessage oobCtrlMsg) {
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public synchronized void onStreamEvent(Notify notify) {
		log.debug("onStreamEvent: {}", notify);
		ObjectMap<?, ?> map = (ObjectMap<?, ?>) notify.getCall().getArguments()[0];
		String code = (String) map.get("code");
		log.debug("<:{}", code);
		if (StatusCodes.NS_PUBLISH_START.equals(code)) {
			state = PUBLISHED;
			rtmpClient.invoke("FCPublish", new Object[] { publishName }, this);
			while (frameBuffer.size() > 0) {
				rtmpClient.publishStreamData(streamId, frameBuffer.remove(0));
			}
		}
	}

	public synchronized void resultReceived(IPendingServiceCall call) {
		log.debug("resultReceived:> {}", call.getServiceMethodName());
		if ("connect".equals(call.getServiceMethodName())) {
			state = STREAM_CREATING;
			rtmpClient.createStream(this);
		} else if ("createStream".equals(call.getServiceMethodName())) {
			state = PUBLISHING;
			Object result = call.getResult();
			if (result instanceof Integer) {
				Integer streamIdInt = (Integer) result;
				streamId = streamIdInt.intValue();
				log.debug("Publishing: {}", state);
				rtmpClient.publish(streamIdInt.intValue(), publishName, publishMode, this);
			} else {
				rtmpClient.disconnect();
				state = STOPPED;
			}
		}
	}
}
