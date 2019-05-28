package com.sms.server.net.rtmp;

import java.lang.ref.WeakReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.service.IPendingServiceCall;
import com.sms.server.net.rtmp.event.Invoke;

/**
 * Can be returned to delay returning the result of invoked methods.
 */
public class DeferredResult {
    /**
     * Logger
     */
	protected static Logger log = LoggerFactory.getLogger(DeferredResult.class);
    /**
     * Weak reference to used channel
     */
	private WeakReference<Channel> channel;
    /**
     * Pending call object
     */
    private IPendingServiceCall call;
    /**
     * Invocation id
     */
    private int invokeId;
    /**
     * Results sent flag
     */
    private boolean resultSent = false;
	
	/**
	 * Set the result of a method call and send to the caller.
	 * 
	 * @param result deferred result of the method call
	 */
	public void setResult(Object result) {
		if (this.resultSent)
			throw new RuntimeException("You can only set the result once.");

		this.resultSent = true;
		Channel channel = this.channel.get();
		if (channel == null) {
			log.warn("The client is no longer connected.");
			return;
		}
		
		Invoke reply = new Invoke();
		call.setResult(result);
		reply.setCall(call);
		reply.setInvokeId(invokeId);
		channel.write(reply);
		channel.getConnection().unregisterDeferredResult(this);
	}

	/**
	 * Check if the result has been sent to the client.
	 * 
	 * @return <code>true</code> if the result has been sent, otherwise <code>false</code> 
	 */
	public boolean wasSent() {
		return resultSent;
	}
	
	/**
     * Setter for invoke Id.
     *
     * @param id  Invocation object identifier
     */
    protected void setInvokeId(int id) {
		this.invokeId = id;
	}
	
	/**
     * Setter for service call.
     *
     * @param call  Service call
     */
    protected void setServiceCall(IPendingServiceCall call) {
		this.call = call;
	}
	
	/**
     * Setter for channel.
     *
     * @param channel  Channel
     */
    protected void setChannel(Channel channel) {
		this.channel = new WeakReference<Channel>(channel);
	}
}
