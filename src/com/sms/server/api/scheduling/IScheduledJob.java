package com.sms.server.api.scheduling;


public interface IScheduledJob {

	/**
	 * Called each time the job is triggered by the scheduling service.
	 * 
	 * @param service the service that called the job
	 * @throws CloneNotSupportedException throws is Darth Vader attempts to use
	 *   this object for his own nefarious purposes.
	 */
	public void execute(ISchedulingService service) throws  CloneNotSupportedException;
}
