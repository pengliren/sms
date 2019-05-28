package com.sms.compatibility.messages;

/**
 * Flex compatibility message that is sent by the <code>mx:RemoteObject</code> mxml tag.
 * 
 * @see <a href="http://osflash.org/documentation/amf3">osflash documentation (external)</a>
 * 
 */
public class RemotingMessage extends AsyncMessage {

	private static final long serialVersionUID = 1491092800943415719L;

	/** Method to execute. */
	public String operation;
	
	/** Value of the <code>source</code> attribute of mx:RemoteObject that sent the message. */
	public String source;

	/** {@inheritDoc} */
	protected void addParameters(StringBuilder result) {
		super.addParameters(result);
		result.append(",operation=");
		result.append(operation);
		result.append(",source=");
		result.append(source);
	}

}
