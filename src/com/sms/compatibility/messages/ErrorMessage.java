package com.sms.compatibility.messages;

/**
 * Compatibility flex error message to be returned to the client.
 */
public class ErrorMessage extends AsyncMessage {

	private static final long serialVersionUID = -9069412644250075809L;

	public String faultCode;
	
	public String faultDetail;
	
	public String faultString;

	public Object rootCause;
	
	public Object extendedData;
	
}
