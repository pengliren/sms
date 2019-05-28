package com.sms.server.net.rtmp.event;
import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.api.service.IPendingServiceCall;

/**
 * Remote invocation event
 */
public class Invoke extends Notify {
	
	private static final long serialVersionUID = -769677790148010729L;

	/** Constructs a new Invoke. */
    public Invoke() {
		super();
	}

	/** {@inheritDoc} */
    @Override
	public byte getDataType() {
		return TYPE_INVOKE;
	}

    /**
     * Create new invocation event with given data
     * @param data        Event data
     */
    public Invoke(IoBuffer data) {
		super(data);
	}

    /**
     * Create new invocation event with given pending service call
     * @param call         Pending call
     */
    public Invoke(IPendingServiceCall call) {
		super(call);
	}

	/** {@inheritDoc} */
    @Override
	public IPendingServiceCall getCall() {
		return (IPendingServiceCall) call;
	}

	/** {@inheritDoc} */
    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Invoke: ").append(call);
		return sb.toString();
	}

	/** {@inheritDoc} */
    @Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Invoke)) {
			return false;
		}
		return super.equals(obj);
	}

}
