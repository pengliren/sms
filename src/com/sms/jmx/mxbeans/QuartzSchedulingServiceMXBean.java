package com.sms.jmx.mxbeans;

import java.util.List;

import javax.management.MXBean;

@MXBean
public interface QuartzSchedulingServiceMXBean {

	/**
	 * Getter for job name.
	 *
	 * @return  Job name
	 */
	public String getJobName();

	public void removeScheduledJob(String name);

	public List<String> getScheduledJobNames();

}
