package com.sms.server.api.scheduling;

import java.util.Date;
import java.util.List;

import org.quartz.Trigger;

public interface ISchedulingService {

	/**
	 * Schedule a job for periodic execution.
	 * 
	 * @param interval time in milliseconds between two notifications of the job
	 * @param job the job to trigger periodically
	 * @return the name of the scheduled job
	 */
	public String addScheduledJob(int interval, IScheduledJob job);

	/**
	 * Schedule a job for single execution in the future.  Please note
	 * that the jobs are not saved if Red5 is restarted in the meantime.
	 * 
	 * @param timeDelta time delta in milliseconds from the current date
	 * @param job the job to trigger
	 * @return the name of the scheduled job
	 */
	public String addScheduledOnceJob(long timeDelta, IScheduledJob job);

	/**
	 * Schedule a job for single execution at a given date.  Please note
	 * that the jobs are not saved if Red5 is restarted in the meantime.  
	 * 
	 * @param date date when the job should be executed
	 * @param job the job to trigger
	 * @return the name of the scheduled job
	 */
	public String addScheduledOnceJob(Date date, IScheduledJob job);
	
	/**
	 * Schedule a job for periodic execution which will start after the specifed delay.
	 * 
	 * @param interval  time in milliseconds between two notifications of the job
	 * @param job the job to trigger periodically
	 * @param delay time in milliseconds to pass before first execution.
	 * @return
	 * 			the name of the scheduled job
	 */
	public String addScheduledJobAfterDelay(int interval, IScheduledJob job, int delay);
	
	public String addScheduledCustomJob(Trigger trigger, IScheduledJob job);

	/**
	 * Pauses the trigger which initiates job execution.
	 * 
 	 * @param name name of the job to stop
	 */
	public void pauseScheduledJob(String name);
	
	/**
	 * Resumes the trigger which initiates job execution.
	 * 
 	 * @param name name of the job to stop
	 */
	public void resumeScheduledJob(String name);	
	
	/**
	 * Stop executing a previously scheduled job.
	 * 
	 * @param name name of the job to stop
	 */
	public void removeScheduledJob(String name);

	/**
	 * Return names of scheduled jobs.
	 * 
	 * @return list of job names
	 */
	public List<String> getScheduledJobNames();
}
