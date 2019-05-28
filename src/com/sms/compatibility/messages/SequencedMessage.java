package com.sms.compatibility.messages;

/**
 * Response to <code>DataMessage</code> requests.
 * 
 * @see DataMessage
 */
public class SequencedMessage extends AsyncMessage {

	private static final long serialVersionUID = 5607350918278510061L;

	public long sequenceId;
	
	public Object sequenceProxies;
	
	public long sequenceSize;
	
	public String dataMessage;

	/** {@inheritDoc} */
	protected void addParameters(StringBuilder result) {
		super.addParameters(result);
		result.append(",sequenceId="+sequenceId);
		result.append(",sequenceProxies="+sequenceProxies);
		result.append(",sequenceSize="+sequenceSize);
		result.append(",dataMessage="+dataMessage);
	}

}
