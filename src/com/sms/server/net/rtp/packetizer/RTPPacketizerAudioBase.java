package com.sms.server.net.rtp.packetizer;

/**
 * RTP Packetizer Audio Base
 * @author pengliren
 *
 */
public abstract class RTPPacketizerAudioBase extends RTPPacketizerBase {

	public RTPPacketizerAudioBase() {
		
		baseType = "aud";
	}
}
