package com.sms.server.net.rtmp.event;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.io.utils.HexDump;

/**
 * Unknown event
 */
public class Unknown extends BaseEvent {
	private static final long serialVersionUID = -1352770037962252975L;
    /**
     * Event data
     */
	protected IoBuffer data;
    /**
     * Type of data
     */
	protected byte dataType;

	public Unknown() {}
    /**
     * Create new unknown event with given data and data type
     * @param dataType             Data type
     * @param data                 Event data
     */
    public Unknown(byte dataType, IoBuffer data) {
		super(Type.SYSTEM);
		this.dataType = dataType;
		this.data = data;
	}

	/** {@inheritDoc} */
    @Override
	public byte getDataType() {
		return dataType;
	}

	/**
     * Getter for data
     *
     * @return  Data
     */
    public IoBuffer getData() {
		return data;
	}

	/** {@inheritDoc} */
    @Override
	public String toString() {
		final IoBuffer buf = getData();
		StringBuffer sb = new StringBuffer();
		sb.append("Size: ");
		sb.append(buf.remaining());
		sb.append(" Data:\n\n");
		sb.append(HexDump.formatHexDump(buf.getHexDump()));
		return sb.toString();
	}

	/** {@inheritDoc} */
    @Override
	protected void releaseInternal() {
		if (data != null) {
			data.free();
			data = null;
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		dataType = in.readByte();
		byte[] byteBuf = (byte[]) in.readObject();
		if (byteBuf != null) {
			data = IoBuffer.allocate(0);
			data.setAutoExpand(true);
			SerializeUtils.ByteArrayToByteBuffer(byteBuf, data);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeByte(dataType);
		if (data != null) {
			out.writeObject(SerializeUtils.ByteBufferToByteArray(data));
		} else {
			out.writeObject(null);
		}
	}
}
