package com.sms.server.stream;

import com.sms.server.api.IScope;
import com.sms.server.api.IScopeHandler;
import com.sms.server.api.stream.IStream;
import com.sms.server.api.stream.IStreamAwareScopeHandler;
import com.sms.server.api.stream.IStreamCodecInfo;
import com.sms.server.api.stream.StreamState;
import com.sms.server.net.rtmp.event.Notify;

/**
 * Abstract base implementation of IStream. Contains codec information, stream name, scope, event handling
 * meand, provides stream start and stop operations.
 */
public abstract class AbstractStream implements IStream {
    
    /**
     * Current state
     */
    protected StreamState state = StreamState.UNINIT;
    
	/**
     *  Stream name
     */
    private String name;
    
    /**
     *  Stream audio and video codec information
     */
	private IStreamCodecInfo codecInfo;
    
	/**
	 * Stores the streams metadata
	 */
	protected Notify metaData;
	
	/**
     *  Stream scope
     */
	private IScope scope;
	
	/**
	 * Timestamp the stream was created.
	 */
	protected long creationTime;
	
    /**
     *  Return stream name
     *  @return     Stream name
     */
	public String getName() {
		return name;
	}

    /**
     * Return codec information
     * @return              Stream codec information
     */
    public IStreamCodecInfo getCodecInfo() {
		return codecInfo;
	}

	/**
	 * Returns the metadata for the associated stream, if it exists.
	 * 
	 * @return stream meta data
	 */
	public Notify getMetaData() {
		return metaData;
	}    
    
    /**
     * Return scope
     * @return         Scope
     */
    public IScope getScope() {
		return scope;
	}
    
	/**
	 * Returns timestamp at which the stream was created.
	 * 
	 * @return creation timestamp
	 */
	public long getCreationTime() {
		return creationTime;
	}

    /**
     * Setter for name
     * @param name     Stream name
     */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * Setter for codec info
     * @param codecInfo     Codec info
     */
    public void setCodecInfo(IStreamCodecInfo codecInfo) {
		this.codecInfo = codecInfo;
	}

    /**
     * Setter for scope
     * @param scope         Scope
     */
	public void setScope(IScope scope) {
		this.scope = scope;
	}

    /**
     * Return stream aware scope handler or null if scope is null
     * @return      IStreamAwareScopeHandler implementation
     */
	protected IStreamAwareScopeHandler getStreamAwareHandler() {
		if (scope != null) {
			IScopeHandler handler = scope.getHandler();
			if (handler instanceof IStreamAwareScopeHandler) {
				return (IStreamAwareScopeHandler) handler;
			}
		}
		return null;
	}
}
