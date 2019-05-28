package com.sms.compatibility.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.amf3.ByteArray;
import com.sms.io.amf3.IDataInput;
import com.sms.io.amf3.IDataOutput;
import com.sms.io.utils.RandomGUID;

/**
 * Base class for for asynchronous Flex compatibility messages.
 */
public class AsyncMessage extends AbstractMessage {

	private static final long serialVersionUID = -3549535089417916783L;
	
	protected static byte CORRELATION_ID_FLAG = 1;

	protected static byte CORRELATION_ID_BYTES_FLAG = 2;

	/** Id of message this message belongs to. */
	public String correlationId;

	protected byte[] correlationIdBytes;

	/** {@inheritDoc} */
	protected void addParameters(StringBuilder result) {
		super.addParameters(result);
		result.append(",correlationId=");
		result.append(correlationId);
	}

	public void setCorrelationId(String id) {
		correlationId = id;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	static Logger log = LoggerFactory.getLogger(AsyncMessage.class);	
	
	@Override
	public void readExternal(IDataInput in) {
		super.readExternal(in);
		short[] flagsArray = readFlags(in);
		for (int i = 0; i < flagsArray.length; ++i) {
			short flags = flagsArray[i];
			short reservedPosition = 0;
			if (i == 0) {
				if ((flags & CORRELATION_ID_FLAG) != 0) {
					correlationId = ((String) in.readObject());
				}
				if ((flags & CORRELATION_ID_BYTES_FLAG) != 0) {
					ByteArray ba = (ByteArray) in.readObject();
					correlationIdBytes = new byte[ba.length()];
					ba.readBytes(correlationIdBytes);
					correlationId = RandomGUID.fromByteArray(correlationIdBytes);
				}
				reservedPosition = 2;
			}
			if (flags >> reservedPosition == 0) {
				continue;
			}
			for (short j = reservedPosition; j < 6; j = (short) (j + 1)) {
				if ((flags >> j & 0x1) == 0) {
					continue;
				}
				in.readObject();
			}
		}
	}	
	
	@Override
	public void writeExternal(IDataOutput output) {
   		super.writeExternal(output);
		if (this.correlationIdBytes == null) {
			this.correlationIdBytes = RandomGUID.toByteArray(this.correlationId);
		}
		short flags = 0;
		if ((this.correlationId != null) && (this.correlationIdBytes == null)) {
			flags = (short) (flags | CORRELATION_ID_FLAG);
		}
		if (this.correlationIdBytes != null) {
			flags = (short) (flags | CORRELATION_ID_BYTES_FLAG);
		}
		output.writeByte((byte) flags);
		if ((this.correlationId != null) && (this.correlationIdBytes == null)) {
			output.writeObject(this.correlationId);
		}
		if (this.correlationIdBytes != null) {
			output.writeObject(this.correlationIdBytes);
		}
	}	
	
}
