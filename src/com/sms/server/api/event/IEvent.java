package com.sms.server.api.event;

/**
 * 
 * @ClassName: IEvent
 * @Description: IEvent interfaces is the essential interface every Event should implement
 * @author pengliren
 */
public interface IEvent {

	/**
	 * Returns even type
	 * 
	 * @return Event type enumeration
	 */
	public Type getType();
	
	/**
	 * Returns event context object
	 * 
	 * @return Event context object
	 */
	public Object getObject();
	
	/**
	 * Whether event has source (event listener(s))
	 * @return	<code>true</code> if so, <code>false</code> otherwise
	 */
	public boolean hasSource();
	
	/**
	 * Returns event listener
	 * @return	Event listener object
	 */
	public IEventListener getSource();

	enum Type {
		SYSTEM, STATUS, SERVICE_CALL, SHARED_OBJECT, STREAM_CONTROL, STREAM_DATA, CLIENT, CLIENT_INVOKE, CLIENT_NOTIFY, SERVER
	}
}
