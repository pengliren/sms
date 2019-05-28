package com.sms.server.messaging;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

/**
 * Event object corresponds to the connect/disconnect events
 * among providers/consumers and pipes.
 */
public class PipeConnectionEvent extends EventObject {
	private static final long serialVersionUID = 9078843765378168072L;

	private List<Runnable> taskList = new ArrayList<Runnable>(3);

	/**
	 * A provider connects as pull mode.
	 */
	public static final int PROVIDER_CONNECT_PULL = 0;

	/**
	 * A provider connects as push mode.
	 */
	public static final int PROVIDER_CONNECT_PUSH = 1;

	/**
	 * A provider disconnects.
	 */
	public static final int PROVIDER_DISCONNECT = 2;

	/**
	 * A consumer connects as pull mode.
	 */
	public static final int CONSUMER_CONNECT_PULL = 3;

	/**
	 * A consumer connects as push mode.
	 */
	public static final int CONSUMER_CONNECT_PUSH = 4;

	/**
	 * A consumer disconnects.
	 */
	public static final int CONSUMER_DISCONNECT = 5;

    /**
     * Provider
     */
    private transient IProvider provider;

    /**
     * Consumer
     */
    private transient IConsumer consumer;

    /**
     * Event type
     */
    private int type;

    /**
     * Parameters map
     */
    private Map<String, Object> paramMap;

	/**
	 * Construct an object with the specific pipe as the
	 * <tt>source</tt>
	 * @param source A pipe that triggers this event.
	 */
	public PipeConnectionEvent(Object source) {
		super(source);
	}

    /**
     * Return pipe connection provider
     * @return          Provider
     */
    public IProvider getProvider() {
		return provider;
	}

    /**
     * Setter for pipe connection provider
     * @param provider  Provider
     */
    public void setProvider(IProvider provider) {
		this.provider = provider;
	}

    /**
     * Return pipe connection consumer
     * @return          Consumer
     */
    public IConsumer getConsumer() {
		return consumer;
	}

    /**
     * Setter for pipe connection consumer
     * @param consumer  Consumer
     */
    public void setConsumer(IConsumer consumer) {
		this.consumer = consumer;
	}

    /**
     * Return event type
     * @return             Event type
     */
    public int getType() {
		return type;
	}

    /**
     * Setter for event type
     * @param type         Event type
     */
    public void setType(int type) {
		this.type = type;
	}

    /**
     * Return event parameters as Map
     * @return             Event parameters as Map
     */
    public Map<String, Object> getParamMap() {
		return paramMap;
	}

    /**
     * Setter for event parameters map
     * @param paramMap     Event parameters as Map
     */
    public void setParamMap(Map<String, Object> paramMap) {
		this.paramMap = paramMap;
	}

    /**
     * Add task to list
     * @param task     Task to add
     */
    public void addTask(Runnable task) {
		taskList.add(task);
	}

    /**
     * Return list of tasks
     * @return       List of tasks
     */
    List<Runnable> getTaskList() {
		return taskList;
	}
}
