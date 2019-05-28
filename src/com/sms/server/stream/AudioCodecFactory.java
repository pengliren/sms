package com.sms.server.stream;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.stream.IAudioStreamCodec;

/**
 * Factory for audio codecs. Creates and returns audio codecs
 */
public class AudioCodecFactory {
    /**
     * Object key
     */
	public static final String KEY = "audioCodecFactory";
	
    /**
     * Logger for audio factory
     */
	private static Logger log = LoggerFactory.getLogger(AudioCodecFactory.class);
    
	/**
     * List of available codecs
     */
	private static List<IAudioStreamCodec> codecs = new ArrayList<IAudioStreamCodec>(1);

	/**
     * Setter for codecs
     *
     * @param codecs List of codecs
     */
    public void setCodecs(List<IAudioStreamCodec> codecs) {
    	AudioCodecFactory.codecs = codecs;
	}

    /**
     * Create and return new audio codec applicable for byte buffer data
     * @param data                 Byte buffer data
     * @return                     audio codec
     */
	public static IAudioStreamCodec getAudioCodec(IoBuffer data) {
		IAudioStreamCodec result = null;
		//get the codec identifying byte
		int codecId = (data.get() & 0xf0) >> 4;		
		try {
    		switch (codecId) {
    			case 10: //aac 
    				result = (IAudioStreamCodec) Class.forName("com.sms.server.stream.codec.AACAudio").newInstance();
    				break;
    			default:
    				result = (IAudioStreamCodec) Class.forName("com.sms.server.stream.codec.AudioStreamCodec").newInstance();
    				break;
    		}
		} catch (Exception ex) {
			log.error("Error creating codec instance", ex);			
		}
		data.rewind();
		//if codec is not found do the old-style loop
		if (result == null) {
    		for (IAudioStreamCodec storedCodec: codecs) {
    			IAudioStreamCodec codec;
    			// XXX: this is a bit of a hack to create new instances of the
    			// configured audio codec for each stream
    			try {
    				codec = storedCodec.getClass().newInstance();
    			} catch (Exception e) {
    				log.error("Could not create audio codec instance", e);
    				continue;
    			}
    			if (codec.canHandleData(data)) {
    				result = codec;
    				break;
    			}
    		}
		}
		return result;
	}

}
