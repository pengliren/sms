package com.sms.server.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sms.server.Configuration;

/**
 * System Cache Time 
 * @author pengliren
 * 
 */
public class SystemTimer {

	private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("SystemTimerExecutor-"));

	private static final long tickUnit = Configuration.NOTIFY_SYSTIMER_TICK;
	private static volatile long time = System.currentTimeMillis();

	private static class TimerTicker implements Runnable {
		public void run() {
			time = System.currentTimeMillis();
		}
	}

	public static long currentTimeMillis() {
		return time;
	}

	static {
		executor.scheduleAtFixedRate(new TimerTicker(), tickUnit, tickUnit, TimeUnit.MILLISECONDS);
	}
}
