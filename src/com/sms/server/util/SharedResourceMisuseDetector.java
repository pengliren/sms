package com.sms.server.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharedResourceMisuseDetector {

	private static final int MAX_ACTIVE_INSTANCES = 256;
	private static final Logger logger = LoggerFactory.getLogger(SharedResourceMisuseDetector.class);
    private final Class<?> type;
    private final AtomicLong activeInstances = new AtomicLong();
    private final AtomicBoolean logged = new AtomicBoolean();

    public SharedResourceMisuseDetector(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        this.type = type;
    }

    public void increase() {
        if (activeInstances.incrementAndGet() > MAX_ACTIVE_INSTANCES) {
            if (logger.isWarnEnabled()) {
                if (logged.compareAndSet(false, true)) {
                    logger.warn(
                            "You are creating too many " + type.getSimpleName() +
                            " instances.  " + type.getSimpleName() +
                            " is a shared resource that must be reused across the" +
                            " application, so that only a few instances are created.");
                }
            }
        }
    }

    public void decrease() {
        activeInstances.decrementAndGet();
    }
}
