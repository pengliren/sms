package com.sms.io.ts;

/**
 * MPEG2TS Parse Exception
 * @author pengliren
 *
 */
public class MPEG2TSParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MPEG2TSParseException() {
		
		super("MPEG2TSParseException");
	}
	
	public MPEG2TSParseException(String message, Throwable cause) {
        super(message, cause);
    }
	
    public MPEG2TSParseException(String message) {
        super(message);
    }

    public MPEG2TSParseException(Throwable cause) {
        super(cause);
    }
}
