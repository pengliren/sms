package com.sms.compatibility.messages;

/**
 * Message containing data update requests.
 */
public class DataMessage extends AsyncMessage {

	private static final long serialVersionUID = -4650851055941106677L;

	public Object identity;
	
	public int operation;

	/** {@inheritDoc} */
	protected void addParameters(StringBuilder result) {
		super.addParameters(result);
		result.append(",identity="+identity);
		result.append(",operation="+operation);
	}

}
