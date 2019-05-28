package com.sms.server.messaging;

/**
 * Signature to mark a provider/consumer never actively providers/consumers
 * messages.
 */
public interface IPassive {
	public static final String KEY = IPassive.class.getName();
}
