package com.sms.server.net.http.codec;

/**
 * Decode State
 * @author pengliren
 *
 */
public class DecodeState {

	public static final byte ENOUGH = 0;
	public static final byte NOT_ENOUGH = 1;

	private byte state;

	private Object object;

	public DecodeState() {
		this.state = ENOUGH;
	}
	
	public byte getState() {
		return state;
	}

	public void setState(byte state) {
		this.state = state;
	}
	
	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

}
