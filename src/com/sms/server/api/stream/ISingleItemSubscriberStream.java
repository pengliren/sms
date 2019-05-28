package com.sms.server.api.stream;

/**
 * A subscriber stream that has only one item for play.
 */
public interface ISingleItemSubscriberStream extends ISubscriberStream {
	/**
     * Setter for property 'playItem'.
     *
     * @param item Value to set for property 'playItem'.
     */
    void setPlayItem(IPlayItem item);
}
