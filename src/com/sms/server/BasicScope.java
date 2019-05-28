package com.sms.server;

import java.beans.ConstructorProperties;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

import com.sms.server.api.IBasicScope;
import com.sms.server.api.IScope;
import com.sms.server.api.ScopeUtils;
import com.sms.server.api.event.IEvent;
import com.sms.server.api.event.IEventListener;
import com.sms.server.api.scheduling.IScheduledJob;
import com.sms.server.api.scheduling.ISchedulingService;
import com.sms.server.scheduling.QuartzSchedulingService;

public abstract class BasicScope extends PersistableAttributeStore implements IBasicScope{

	/**
	 * Parent scope. Scopes can be nested.
	 *
	 * @see org.red5.server.api.IScope
	 */
	protected IScope parent;
	
	/**
	 * Creation timestamp
	 */
	protected long creation;

	/**
	 * List of event listeners
	 */
	protected CopyOnWriteArraySet<IEventListener> listeners;

	/**
	 * Scope persistence storage type
	 */
	protected String persistenceClass;

	/**
	 * Set to true to prevent the scope from being freed upon disconnect.
	 */
	protected boolean keepOnDisconnect = false;

	/**
	 * Set to amount of time (in seconds) the scope will be kept before being freed,
	 * after the last disconnect.
	 */
	protected int keepDelay = 0;

	/**
	 * Creates unnamed scope
	 */
	@ConstructorProperties(value = { "" })
	public BasicScope() {		
		this(null, "scope", null, false);
	}

	/**
	 * Constructor for basic scope
	 *
	 * @param parent           Parent scope
	 * @param type             Scope type
	 * @param name             Scope name. Used to identify scopes in application, must be unique among scopes of one level
	 * @param persistent       Whether scope is persistent
	 */
	@ConstructorProperties({ "parent", "type", "name", "persistent" })
	public BasicScope(IScope parent, String type, String name, boolean persistent) {
		super(type, name, null, persistent);
		this.parent = parent;
		this.listeners = new CopyOnWriteArraySet<IEventListener>();
		this.creation = System.nanoTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasParent() {
		return true;
	}

	/**
	 *{@inheritDoc}
	 */
	public IScope getParent() {
		return parent;
	}

	/**
	 *{@inheritDoc}
	 */
	public int getDepth() {
		return parent.getDepth() + 1;
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public String getPath() {
		return parent.getPath() + '/' + parent.getName();
	}

	/**
	 * Sets the amount of time to keep the scope available after the
	 * last disconnect.
	 * 
	 * @param keepDelay delay
	 */
	public void setKeepDelay(int keepDelay) {
		this.keepDelay = keepDelay;
	}

	/**
	 * Add event listener to list of notified objects
	 * @param listener        Listening object
	 * @return true if listener is added and false otherwise
	 */
	public boolean addEventListener(IEventListener listener) {
		return listeners.add(listener);
	}

	/**
	 * Remove event listener from list of listeners
	 * @param listener            Listener to remove
	 * @return true if listener is removed and false otherwise
	 */
	public boolean removeEventListener(IEventListener listener) {
		boolean removed = listeners.remove(listener);
		if (!keepOnDisconnect) {
			if (ScopeUtils.isRoom(this) && listeners.isEmpty()) {
				if (keepDelay > 0) {
					// create a job to keep alive for n seconds
					ISchedulingService schedulingService = QuartzSchedulingService.getInstance();/*(ISchedulingService) parent.getContext().getBean(ISchedulingService.BEAN_NAME)*/
					schedulingService.addScheduledOnceJob(keepDelay * 1000, new KeepAliveJob(this));
				} else {
					// delete empty rooms
					parent.removeChildScope(this);
				}
			}
		}
		return removed;
	}

	/**
	 * Return listeners list iterator
	 *
	 * @return  Listeners list iterator
	 */
	public Iterator<IEventListener> getEventListeners() {
		return listeners.iterator();
	}

	/**
	 * Returns true if there are event listeners attached to
	 * this scope.
	 * 
	 * @return true if it has listeners; else false.
	 */
	public boolean hasEventListeners() {
		return !listeners.isEmpty();
	}

	/**
	 * Handles event. To be implemented in subclass realization
	 *
	 * @param event          Event context
	 * @return               Event handling result
	 */
	public boolean handleEvent(IEvent event) {
		// do nothing.
		return false;
	}

	/**
	 * Notifies listeners on event. Current implementation is empty. To be implemented in subclass realization
	 * @param event      Event to broadcast
	 */
	public void notifyEvent(IEvent event) {

	}

	/**
	 * Dispatches event (notifies all listeners)
	 *
	 * @param event        Event to dispatch
	 */
	public void dispatchEvent(IEvent event) {
		for (IEventListener listener : listeners) {
			if (event.getSource() == null || event.getSource() != listener) {
				listener.notifyEvent(event);
			}
		}
	}
	
	/**
	 * Hash code is based on the scope's name and type
	 * 
	 * @return hash code
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/**
	 * Equality is based on the scope's name and type
	 * 
	 * @param obj
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BasicScope other = (BasicScope) obj;
		if (hashCode() != other.hashCode()) {
			return false;
		}
		return true;
	}

	public int compareTo(BasicScope that) {
		if (this.equals(that)) {
			return 0;
		}
		return name.compareTo(that.getName());
	}

	/**
	 * Getter for subscopes list iterator. Returns null because this is a base implementation
	 *
	 * @return           Iterator for subscopes
	 */
	public Iterator<IBasicScope> iterator() {
		return null;
	}

	/**
	 * Iterator for basic scope
	 */
	public static class EmptyBasicScopeIterator implements Iterator<IBasicScope> {

		/** {@inheritDoc} */
		public boolean hasNext() {
			return false;
		}

		/** {@inheritDoc} */
		public IBasicScope next() {
			return null;
		}

		/** {@inheritDoc} */
		public void remove() {
			// nothing
		}

	}

	/**
	 * Keeps the scope alive for a set number of seconds. This should
	 * fulfill the APPSERVER-165 improvement.
	 */
	private class KeepAliveJob implements IScheduledJob {

		private IBasicScope scope = null;

		KeepAliveJob(IBasicScope scope) {
			this.scope = scope;
		}

		public void execute(ISchedulingService service) {
			if (listeners.isEmpty()) {
				// delete empty rooms
				parent.removeChildScope(scope);
			}
		}

	}
}
