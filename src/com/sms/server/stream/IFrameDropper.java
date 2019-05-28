package com.sms.server.stream;

import com.sms.server.stream.message.RTMPMessage;

/**
 * Interface for classes that implement logic to drop frames.
 */
public interface IFrameDropper {

	/** Send keyframes, interframes and disposable interframes. */
    public static final int SEND_ALL = 0;

	/** Send keyframes and interframes. */
    public static final int SEND_INTERFRAMES = 1;

	/** Send keyframes only. */
    public static final int SEND_KEYFRAMES = 2;

	/** Send keyframes only and switch to SEND_INTERFRAMES later. */
    public static final int SEND_KEYFRAMES_CHECK = 3;

	/**
	 * Checks if a message may be sent to the subscriber.
	 * 
	 * @param message
	 * 			the message to check
	 * @param pending
	 * 			the number of pending messages
	 * @return <code>true</code> if the packet may be sent, otherwise
	 *         <code>false</code>
	 */
	boolean canSendPacket(RTMPMessage message, long pending);

	/**
	 * Notify that a packet has been dropped.
	 * 
	 * @param message
	 * 			the message that was dropped
	 */
	void dropPacket(RTMPMessage message);

	/**
	 * Notify that a message has been sent.
	 * 
	 * @param message
	 * 			the message that was sent
	 */
	void sendPacket(RTMPMessage message);

	/** Reset the frame dropper. */
	void reset();

	/**
	 * Reset the frame dropper to a given state.
	 * 
	 * @param state
	 * 			the state to reset the frame dropper to
	 */
	void reset(int state);

}
