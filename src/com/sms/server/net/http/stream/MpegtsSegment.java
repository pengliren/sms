package com.sms.server.net.http.stream;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.io.utils.HexDump;
import com.sms.server.util.SystemTimer;

/**
 * MPEG2TS Segment Data
 * @author pengliren
 *
 */
public class MpegtsSegment {

	// name of the segment
	private String name;

	// segment seq number
	private int sequence;

	// creation time
	private long created = SystemTimer.currentTimeMillis();

	// queue for holding data if using memory mapped i/o
	private volatile IoBuffer buffer;

	// lock used when writing or slicing the buffer
	private volatile ReentrantLock lock = new ReentrantLock();

	// whether or not the segment is closed
	private volatile boolean closed = false;	
	
	private String encKey;
	
	private byte[] encKeyBytes;

	public MpegtsSegment(String name, int sequence) {
		this.name = name;
		this.sequence = sequence;
		buffer = IoBuffer.allocate(1024 * 1024, false);
		buffer.setAutoExpand(true);		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public int getSequence() {
		return sequence;
	}

	public long getCreated() {
		return created;
	}

	public void setBuffer(IoBuffer buf) {
		buffer = buf;
	}

	public IoBuffer getBuffer() {
		return buffer;
	}
	
	public String getEncKey() {
		return encKey;
	}

	public void setEncKey(String encKey) {
		this.encKey = encKey;
		if(encKey != null) this.encKeyBytes = HexDump.decodeHexString(encKey);
	}

	public byte[] getEncKeyBytes() {
		return encKeyBytes;
	}

	public boolean close() {
		boolean result = false;
		if (buffer != null) {
			lock.lock();
			closed = true;
			try {
				buffer.flip();
				result = true;
			} finally {
				lock.unlock();
			}
		}
		return result;
	}

	/**
	 * Should be called only when we are completely finished with this segment
	 * and no longer want it to be available.
	 */
	public void dispose() {
		if (buffer != null) {
			buffer.free();
		}
	}

	public boolean isClosed() {
		return closed;
	}
}
