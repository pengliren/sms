package com.sms.server.api.statistics;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts numbers used by the statistics. Keeps track of current,
 * maximum and total numbers.
 */
public class StatisticsCounter {

	/** Current number. */
	private AtomicInteger current = new AtomicInteger();
	
	/** Total number. */
	private AtomicInteger total = new AtomicInteger();
	
	/** Maximum number. */
	private AtomicInteger max = new AtomicInteger();
	
	/**
	 * Increment statistics by one.
	 */
	public void increment() {
		total.incrementAndGet();
		max.compareAndSet(current.intValue(), current.incrementAndGet());
	}
	
	/**
	 * Decrement statistics by one.
	 */
	public void decrement() {
		current.decrementAndGet();
	}
	
	/**
	 * Get current number.
	 * 
	 * @return current number
	 */
	public int getCurrent() {
		return current.intValue();
	}
	
	/**
	 * Get total number.
	 * 
	 * @return total
	 */
	public int getTotal() {
		return total.intValue();
	}
	
	/**
	 * Get maximum number.
	 * 
	 * @return max
	 */
	public int getMax() {
		return max.intValue();
	}
	
}
