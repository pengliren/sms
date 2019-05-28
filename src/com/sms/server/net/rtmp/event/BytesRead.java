package com.sms.server.net.rtmp.event;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Bytes read event
 */
public class BytesRead extends BaseEvent {
	private static final long serialVersionUID = -127649312402709338L;
    /**
     * Bytes read
     */
	private int bytesRead;
	public BytesRead() {
		super(Type.STREAM_CONTROL);
	}

    /**
     * Creates new event with given bytes number
     * @param bytesRead       Number of bytes read
     */
    public BytesRead(int bytesRead) {
		this();
		this.bytesRead = bytesRead;
	}

	/** {@inheritDoc} */
    @Override
	public byte getDataType() {
		return TYPE_BYTES_READ;
	}

	/**
     * Return number of bytes read
     *
     * @return  Number of bytes
     */
    public int getBytesRead() {
		return bytesRead;
	}

	/**
     * Setter for bytes read
     *
     * @param bytesRead  Number of bytes read
     */
    public void setBytesRead(int bytesRead) {
		this.bytesRead = bytesRead;
	}

    /**
     * Release event (set bytes read to zero)
     */
    protected void doRelease() {
		bytesRead = 0;
	}

	/** {@inheritDoc} */
    @Override
	public String toString() {
		return "StreamBytesRead: " + bytesRead;
	}

	/** {@inheritDoc} */
    @Override
	protected void releaseInternal() {

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		bytesRead = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(bytesRead);
	}
}