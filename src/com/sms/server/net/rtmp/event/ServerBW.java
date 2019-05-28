package com.sms.server.net.rtmp.event;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Server bandwidth event
 */
public class ServerBW extends BaseEvent {
	
	private static final long serialVersionUID = 24487902555977210L;

	/**
	 * Bandwidth
	 */
	private int bandwidth;

	public ServerBW() {
	}

	/**
	 * Server bandwidth event
	 * @param bandwidth      Bandwidth
	 */
	public ServerBW(int bandwidth) {
		super(Type.STREAM_CONTROL);
		this.bandwidth = bandwidth;
	}

	/** {@inheritDoc} */
	@Override
	public byte getDataType() {
		return TYPE_SERVER_BANDWIDTH;
	}

	/**
	 * Getter for bandwidth
	 *
	 * @return  Bandwidth
	 */
	public int getBandwidth() {
		return bandwidth;
	}

	/**
	 * Setter for bandwidth
	 *
	 * @param bandwidth  New bandwidth.
	 */
	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ServerBW: " + bandwidth;
	}

	/** {@inheritDoc} */
	@Override
	protected void releaseInternal() {

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		bandwidth = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(bandwidth);
	}
}
