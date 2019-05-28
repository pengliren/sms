package com.sms.server.messaging;

/**
 * Message component handles out-of-band control messages
 */
public interface IMessageComponent {
	/**
	 * 
	 * @param source               Message component source
	 * @param pipe                 Connection pipe
	 * @param oobCtrlMsg           Out-of-band control message
	 */
	void onOOBControlMessage(IMessageComponent source, IPipe pipe,
			OOBControlMessage oobCtrlMsg);
}
