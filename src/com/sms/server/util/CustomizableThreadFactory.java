package com.sms.server.util;

import java.util.concurrent.ThreadFactory;

public class CustomizableThreadFactory extends CustomizableThreadCreator implements ThreadFactory {

	public CustomizableThreadFactory() {

		super();
	}
	
	public CustomizableThreadFactory(String threadNamePrefix) {

		super(threadNamePrefix);
	}

	@Override
	public Thread newThread(Runnable runnable) {

		return createThread(runnable);
	}

}
