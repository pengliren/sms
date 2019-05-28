package com.sms.server.stream;

/**
 * Throw when stream can't be found
 */
public class StreamNotFoundException extends Exception {
	private static final long serialVersionUID = 812106823615971891L;

	public StreamNotFoundException(String name) {
		super(String.format("Stream %s not found", name));
	}

}