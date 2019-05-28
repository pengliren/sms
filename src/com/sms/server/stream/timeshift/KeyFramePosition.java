package com.sms.server.stream.timeshift;

/**
 * KeyFrame Position
 * @author pengliren
 *
 */
public class KeyFramePosition {

	private int timestamp;

	private long position;

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String toString(){
		return "[timestamp:"+timestamp+",position"+position+"]";
	}
}
