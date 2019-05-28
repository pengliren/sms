package com.sms.server.net.rtmp.event;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * RTMP Abort event.
 *
 */
public class Abort extends BaseEvent {

	private int channelId=0;
	public Abort()
	{
		super(Type.SYSTEM);
	}
	public Abort(int channelId) {
		this.channelId = channelId;
	}
	public byte getDataType() {
		return TYPE_ABORT;
	}

	protected void releaseInternal() {
	
	}
	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}
	public int getChannelId() {
		return channelId;
	}
	/** {@inheritDoc} */
    @Override
	public String toString() {
		return "Abort Channel: " + channelId;
	}
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		channelId= in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(channelId);
	}

}