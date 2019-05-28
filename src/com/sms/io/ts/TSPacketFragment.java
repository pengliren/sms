package com.sms.io.ts;

/**
 * ts packet fragment
 * 
 * @author pengliren
 * 
 */
public class TSPacketFragment {

	protected byte[] buffer = null;
	protected int offset = 0;
	protected int len = 0;

	public TSPacketFragment(byte[] data, int offset, int len) {
		this.buffer = data;
		this.offset = offset;
		this.len = len;
	}

	public TSPacketFragment(byte[] data) {
		this.buffer = data;
		this.offset = 0;
		this.len = data.length;
	}

	public TSPacketFragment() {
	}

	public int size() {
		return this.len;
	}

	public byte[] getBuffer() {
		return this.buffer;
	}

	public void setBuffer(byte[] data) {
		this.buffer = data;
	}

	public int getOffset() {
		return this.offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLen() {
		return this.len;
	}

	public void setLen(int len) {
		this.len = len;
	}
}
