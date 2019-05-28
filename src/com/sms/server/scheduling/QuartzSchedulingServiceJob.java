package com.sms.server.scheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.scheduling.IScheduledJob;
import com.sms.server.api.scheduling.ISchedulingService;

/**
 * Scheduled job that is registered in the Quartz scheduler. 
 */
public class QuartzSchedulingServiceJob implements Job {
    /**
     * Scheduling service constant
     */
    protected static final String SCHEDULING_SERVICE = "scheduling_service";

    /**
     * Scheduled job constant
     */
    protected static final String SCHEDULED_JOB = "scheduled_job";

    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(QuartzSchedulingServiceJob.class);

    /** {@inheritDoc} */
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
		ISchedulingService service = (ISchedulingService) arg0.getJobDetail()
				.getJobDataMap().get(SCHEDULING_SERVICE);
		IScheduledJob job = (IScheduledJob) arg0.getJobDetail().getJobDataMap()
				.get(SCHEDULED_JOB);
        try {
            job.execute(service);
        } catch (Throwable e) {
            log.error("Job {} execution failed", job.toString(), e);
        }
    }

}
