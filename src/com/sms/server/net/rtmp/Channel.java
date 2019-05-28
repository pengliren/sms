package com.sms.server.net.rtmp;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.ScopeContextBean;
import com.sms.server.api.IScope;
import com.sms.server.api.stream.IClientStream;
import com.sms.server.api.stream.IRtmpSampleAccess;
import com.sms.server.net.rtmp.event.IRTMPEvent;
import com.sms.server.net.rtmp.event.Invoke;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.message.Header;
import com.sms.server.net.rtmp.message.Packet;
import com.sms.server.net.rtmp.status.Status;
import com.sms.server.net.rtmp.status.StatusCodes;
import com.sms.server.service.Call;
import com.sms.server.service.PendingCall;

/**
 * Identified connection that transfers packets.
 */
public class Channel {
    /**
     * Logger
     */
	protected static Logger log = LoggerFactory.getLogger(Channel.class);
    /**
     * RTMP connection used to transfer packets.
     */
	private RTMPConnection connection;

    /**
     * Channel id
     */
    private int id;

    /**
     * Creates channel from connection and channel id
     * @param conn                Connection
     * @param channelId           Channel id
     */
	public Channel(RTMPConnection conn, int channelId) {
		connection = conn;
		id = channelId;
	}

    /**
     * Closes channel with this id on RTMP connection.
     */
    public void close() {
    	
    	if (connection == null) {
            return;
        }
		connection.closeChannel(id);
	}

	/**
     * Getter for id.
     *
     * @return  Channel ID
     */
    public int getId() {
		return id;
	}
	
	/**
     * Getter for RTMP connection.
     *
     * @return  RTMP connection
     */
    protected RTMPConnection getConnection() {
		return connection;
	}

    /**
     * Writes packet from event data to RTMP connection.
	 *
     * @param event          Event data
     */
    public void write(IRTMPEvent event) {
    	
    	if (connection == null) {
    		log.warn("Connection is null for channel: {}", id);
            return;
        }
		final IClientStream stream = connection.getStreamByChannelId(id);
		if (id > 3 && stream == null) {
			log.warn("Non-existant stream for channel id: {}, connection id: {} discarding: {}", id, connection.getSessionId());
		}

		// if the stream is non-existant, the event will go out with stream id == 0
		final int streamId = (stream == null) ? 0 : stream.getStreamId();
		write(event, streamId);
	}

    /**
     * Writes packet from event data to RTMP connection and stream id.
	 *
     * @param event           Event data
     * @param streamId        Stream id
     */
    private void write(IRTMPEvent event, int streamId) {

    	if (connection == null) {
            return;
        }
    	
		final Header header = new Header();
		final Packet packet = new Packet(header, event);

		header.setChannelId(id);
		header.setTimer((int)event.getTimestamp());
		header.setStreamId(streamId);
		header.setDataType(event.getDataType());

		// should use RTMPConnection specific method.. 
		connection.write(packet);

	}

    /**
     * Sends status notification.
	 *
     * @param status           Status
     */
    public void sendStatus(Status status) {
		
    	if (connection == null) {
            return;
        }
    	final boolean andReturn = !status.getCode().equals(StatusCodes.NS_DATA_START);
		final Notify event;
		if (andReturn) {
			final PendingCall call = new PendingCall(null, "onStatus", new Object[] { status });
			event = new Invoke();
			if (status.getCode().equals(StatusCodes.NS_PLAY_START)) {	
				IScope scope = connection.getScope();
				if (scope.getContext().hasBean(ScopeContextBean.RTMPSAMPLEACCESS_BEAN)) {
					IRtmpSampleAccess sampleAccess = (IRtmpSampleAccess) scope.getContext().getBean(ScopeContextBean.RTMPSAMPLEACCESS_BEAN);
					boolean videoAccess = sampleAccess == null ? false : sampleAccess.isVideoAllowed(scope);
					boolean audioAccess = sampleAccess == null ? false : sampleAccess.isAudioAllowed(scope);
					final Call call2 = new Call(null, "|RtmpSampleAccess", null);
					Notify notify = new Notify();
					notify.setInvokeId(1);
					notify.setCall(call2);
					notify.setData(IoBuffer.wrap(new byte[] { 0x01, (byte) (audioAccess ? 0x01 : 0x00), 0x01, (byte) (videoAccess ? 0x01 : 0x00) }));
					write(notify, connection.getStreamIdForChannel(id));
				}
			}
			event.setInvokeId(connection.getInvokeId()); 
			event.setCall(call);
		} else {
			final Call call = new Call(null, "onStatus", new Object[] { status });
			event = new Notify();
			event.setInvokeId(connection.getInvokeId()); 
			event.setCall(call);
		}
		// We send directly to the corresponding stream as for
		// some status codes, no stream has been created and thus
		// "getStreamByChannelId" will fail.
		write(event, connection.getStreamIdForChannel(id));
	}

}
