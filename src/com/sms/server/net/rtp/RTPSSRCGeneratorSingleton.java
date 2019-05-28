package com.sms.server.net.rtp;

import java.util.Random;

/**
 * RTP SSRC Generator Singleton
 * @author pengliren
 *
 */
public class RTPSSRCGeneratorSingleton {
	
	private Object lock = new Object();
	private Random rnd = null;
	
	private static final class SingletonHolder {

		private static final RTPSSRCGeneratorSingleton INSTANCE = new RTPSSRCGeneratorSingleton();
	}
	
	private RTPSSRCGeneratorSingleton() {
		
	}

	public static RTPSSRCGeneratorSingleton getInstance() {

		return SingletonHolder.INSTANCE;
	}

	public int getNextSSRC() {
		synchronized (this.lock) {
			if (this.rnd == null)
				this.rnd = new Random(System.currentTimeMillis());
			return this.rnd.nextInt(0x7FFFFFFE) + 1;
		}
	}
}
