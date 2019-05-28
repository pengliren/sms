package com.sms.compatibility.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.amf3.IDataInput;
import com.sms.io.amf3.IDataOutput;
import com.sms.io.utils.RandomGUID;
import com.sms.server.util.SystemTimer;

/**
 * Flex compatibility message that is returned to the client.
 * 
 */
public class AcknowledgeMessage extends AsyncMessage {

	private static final long serialVersionUID = 228072709981643313L;

	static Logger log = LoggerFactory.getLogger(AcknowledgeMessage.class);

	public AcknowledgeMessage() {
		this.messageId = new RandomGUID().toString();
		this.timestamp = SystemTimer.currentTimeMillis();
	}

	@Override
	public void readExternal(IDataInput in) {
		super.readExternal(in);
		short[] flagsArray = readFlags(in);
		for (int i = 0; i < flagsArray.length; ++i) {
			short flags = flagsArray[i];
			short reservedPosition = 0;
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
		output.writeByte((byte) 0);
	}

}
