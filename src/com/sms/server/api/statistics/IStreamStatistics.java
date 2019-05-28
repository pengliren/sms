package com.sms.server.api.statistics;

/**
 * Base class for all stream statistics.
 */
public interface IStreamStatistics extends IStatisticsBase {

	/**
	 * Return the currently active timestamp inside the stream.
	 * 
	 * @return the timestamp in milliseconds
	 */
	public long getCurrentTimestamp();
	
}
