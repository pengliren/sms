package com.sms.server.scheduling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.jmx.JMXAgent;
import com.sms.jmx.mxbeans.QuartzSchedulingServiceMXBean;
import com.sms.server.api.scheduling.IScheduledJob;
import com.sms.server.api.scheduling.ISchedulingService;


/**
 * Scheduling service that uses Quartz as backend.
 */
public class QuartzSchedulingService implements ISchedulingService, QuartzSchedulingServiceMXBean {

	private static Logger log = LoggerFactory.getLogger(QuartzSchedulingService.class);
	
	/**
	 * Number of job details
	 */
	protected AtomicLong jobDetailCounter = new AtomicLong(0);

	/**
	 * Creates schedulers.
	 */
	protected SchedulerFactory factory;
	
	/**
	 * Service scheduler
	 */
	protected Scheduler scheduler;

	/**
	 * Instance id
	 */
	protected String instanceId;
	
	private static final class SingletonHolder {

		private static final QuartzSchedulingService INSTANCE = new QuartzSchedulingService();
	}

	public static QuartzSchedulingService getInstance() {

		return SingletonHolder.INSTANCE;
	}

	private QuartzSchedulingService() {

		try {
			String conf = System.getProperty("sms.config_root");
			factory = new StdSchedulerFactory(String.format("%s%s", conf, "/quartz.properties"));

			if (factory == null) {
				factory = new StdSchedulerFactory();
			}		
			if (instanceId == null) {
				scheduler = factory.getScheduler();
			} else {
				scheduler = factory.getScheduler(instanceId);
			}
			//start the scheduler
			if (scheduler != null) {
				scheduler.start();
			} else {
				log.error("Scheduler was not started");
			}
			
			JMXAgent.registerMBean(this, this.getClass().getName(), QuartzSchedulingServiceMXBean.class);

		} catch (SchedulerException e) {
			log.info("QuartzSchedulingService Exception {}", e.getMessage());
		}
	}	
	
	public void setFactory(SchedulerFactory factory) {
		this.factory = factory;
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	/** {@inheritDoc} */
	public String addScheduledJob(int interval, IScheduledJob job) {
		String result = getJobName();

		// Create trigger that fires indefinitely every <interval> milliseconds
		SimpleTrigger trigger = new SimpleTrigger("Trigger_" + result, null,
				new Date(), null, SimpleTrigger.REPEAT_INDEFINITELY, interval);
		scheduleJob(result, trigger, job);
		return result;
	}

	/** {@inheritDoc} */
	public String addScheduledOnceJob(Date date, IScheduledJob job) {
		String result = getJobName();

		// Create trigger that fires once at <date>
		SimpleTrigger trigger = new SimpleTrigger("Trigger_" + result, null,
				date);
		scheduleJob(result, trigger, job);
		return result;
	}

	/** {@inheritDoc} */
	public String addScheduledOnceJob(long timeDelta, IScheduledJob job) {
		// Create trigger that fires once in <timeDelta> milliseconds
		return addScheduledOnceJob(new Date(System.currentTimeMillis()
				+ timeDelta), job);
	}
	
	/** {@inheritDoc} */
	public String addScheduledJobAfterDelay(int interval, IScheduledJob job, int delay) {
		String result = getJobName();
		// Initialize the start time to now and add the delay.
		long startTime = System.currentTimeMillis() + delay;
		// Create trigger that fires indefinitely every <internval> milliseconds.
		SimpleTrigger trigger = new SimpleTrigger("Trigger_" + result, 
				null, new Date(startTime), null, SimpleTrigger.REPEAT_INDEFINITELY, interval);
		// Schedule the job with Quartz.
		scheduleJob(result, trigger, job);
		// Return the job name.
		return result;
	}
	
	@Override
	public String addScheduledCustomJob(Trigger trigger, IScheduledJob job) {
		
		String result = getJobName();
		trigger.setName("Trigger_" + result);
		scheduleJob(result, trigger, job);
		// Return the job name.
		return result;
	}

	/**
	 * Getter for job name.
	 *
	 * @return  Job name
	 */
	public String getJobName() {
		String result = "ScheduledJob_" + jobDetailCounter.getAndIncrement();
		return result;
	}

	/** {@inheritDoc} */
	public List<String> getScheduledJobNames() {
		List<String> result = new ArrayList<String>();
		if (scheduler != null) {
    		try {
    			for (String name : scheduler.getJobNames(null)) {
    				result.add(name);
    			}
    		} catch (SchedulerException ex) {
    			throw new RuntimeException(ex);
    		}
		} else {
			log.warn("No scheduler is available");
		}
		return result;
	}

	/** {@inheritDoc} */
	public void pauseScheduledJob(String name) {
		try {
			scheduler.pauseJob(name, null);
		} catch (SchedulerException ex) {
			throw new RuntimeException(ex);
		}
	}	
	
	/** {@inheritDoc} */
	public void resumeScheduledJob(String name) {
		try {
			scheduler.resumeJob(name, null);
		} catch (SchedulerException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void pauseScheduledTrigger(String name) {
		try {
			scheduler.pauseTrigger("Trigger_" + name, null);
		} catch (SchedulerException ex) {
			throw new RuntimeException(ex);
		}
	}	
	
	public void resumeScheduledTrigger(String name) {
		try {
			scheduler.resumeTrigger("Trigger_" + name, null);
		} catch (SchedulerException ex) {
			throw new RuntimeException(ex);
		}
	}	
	
	/** {@inheritDoc} */
	public void removeScheduledJob(String name) {
		try {
			scheduler.deleteJob(name, null);
		} catch (SchedulerException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Schedules job
	 * @param name               Job name
	 * @param trigger            Job trigger
	 * @param job                Scheduled job object
	 *
	 * @see org.red5.server.api.scheduling.IScheduledJob
	 */
	private void scheduleJob(String name, Trigger trigger, IScheduledJob job) {
		if (scheduler != null) {
    		// Store reference to applications job and service
    		JobDetail jobDetail = new JobDetail(name, null,
    				QuartzSchedulingServiceJob.class);
    		jobDetail.getJobDataMap().put(QuartzSchedulingServiceJob.SCHEDULING_SERVICE, this);
    		jobDetail.getJobDataMap().put(QuartzSchedulingServiceJob.SCHEDULED_JOB, job);    
    		try {
    			scheduler.scheduleJob(jobDetail, trigger);
    		} catch (SchedulerException ex) {
    			throw new RuntimeException(ex);
    		}
		} else {
			log.warn("No scheduler is available");
		}
	}

	public void destroy() throws Exception {
		if (scheduler != null) {
        	log.debug("Destroying...");
            scheduler.shutdown();
		}
    }
}
