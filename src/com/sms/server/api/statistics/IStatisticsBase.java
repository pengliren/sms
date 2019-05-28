package com.sms.server.api.statistics;

public interface IStatisticsBase {

	/**
	 * Return the timestamp the object was created.
	 * 
	 * @return the timestamp in milliseconds since midnight, January 1, 1970 UTC.
	 */
	public long getCreationTime();
}
