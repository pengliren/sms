package com.sms.server.net.rtmp.event;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Client bandwidth event
 */
public class ClientBW extends BaseEvent {

	private static final long serialVersionUID = 5848656135751336839L;

	/**
	 * Bandwidth
	 */
	private int bandwidth;

	/**
	 * Enforcement level or limit type of the bandwidth value based on three values.
	 * <pre>
	 * TYPE_HARD 0
	 * TYPE_SOFT 1
	 * TYPE_DYNAMIC 2
	 * </pre>
	 */
	private byte limitType;

	public ClientBW() {
		super(Type.STREAM_CONTROL);
	}

	public ClientBW(int bandwidth, byte limitType) {
		this();
		this.bandwidth = bandwidth;
		this.limitType = limitType;
	}

	/** {@inheritDoc} */
	@Override
	public byte getDataType() {
		return TYPE_CLIENT_BANDWIDTH;
	}

	/**
	 * Getter for property 'bandwidth'.
	 *
	 * @return Value for property 'bandwidth'.
	 */
	public int getBandwidth() {
		return bandwidth;
	}

	/**
	 * Setter for bandwidth
	 *
	 * @param bandwidth  New bandwidth
	 */
	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	/**
	 * Getter for limitType
	 *
	 * @return limitType for property 'limitType'.
	 */
	public byte getLimitType() {
		return limitType;
	}

	/**
	 * Setter for property 'limitType'.
	 *
	 * @param limitType Value to set for property 'limitType'.
	 */
	public void setLimitType(byte limitType) {
		this.limitType = limitType;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ClientBW: " + bandwidth + " enforcementLevel: " + limitType;
	}

	/** {@inheritDoc} */
	@Override
	protected void releaseInternal() {

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		bandwidth = in.readInt();
		limitType = in.readByte();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(bandwidth);
		out.writeByte(limitType);
	}
}
