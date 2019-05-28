package com.sms.server.stream;

import java.io.File;
import java.util.List;

import com.sms.server.api.IScope;
import com.sms.server.api.IScopeService;
import com.sms.server.api.stream.IBroadcastStream;
import com.sms.server.messaging.IMessageInput;

/**
 *  Central unit to get access to different types of provider inputs
 */
public interface IProviderService extends IScopeService {

	enum INPUT_TYPE {
		NOT_FOUND, LIVE, LIVE_WAIT, VOD;
	};
	
	/**
	 * Returns the input type for a named provider if a source of input exists.
	 * Live is checked first and VOD second.
	 * 
	 * @param scope         Scope of provider
	 * @param name          Name of provider
	 * @param type          Type of video stream
	 * @return LIVE if live, VOD if VOD, and NOT_FOUND otherwise
	 */
	INPUT_TYPE lookupProviderInput(IScope scope, String name, int type);	
	
	/**
	 * Get a named provider as the source of input.
	 * Live stream first, VOD stream second.
	 * @param scope         Scope of provider
	 * @param name          Name of provider
	 * @return <tt>null</tt> if nothing found.
	 */
	IMessageInput getProviderInput(IScope scope, String name);

	/**
	 * Get a named Live provider as the source of input.
	 * 
	 * @param scope         Scope of provider
	 * @param name          Name of provider
     * @param needCreate    Whether there's need to create basic scope / live provider if they don't exist
	 * @return <tt>null</tt> if not found.
	 */
	IMessageInput getLiveProviderInput(IScope scope, String name, boolean needCreate);

	/**
	 * Get a named VOD provider as the source of input.
	 * 
	 * @param scope         Scope of provider
	 * @param name          Name of provider
	 * @return <tt>null</tt> if not found.
	 */
	IMessageInput getVODProviderInput(IScope scope, String name);

	/**
	 * Get a named VOD source file.
	 * 
	 * @param scope         Scope of provider
	 * @param name          Name of provider
	 * @return <tt>null</tt> if not found.
	 */
	File getVODProviderFile(IScope scope, String name);

	/**
	 * Register a broadcast stream to a scope.
	 * 
	 * @param scope         Scope
	 * @param name          Name of stream
	 * @param stream        Broadcast stream to register
	 * @return <tt>true</tt> if register successfully.
	 */
	boolean registerBroadcastStream(IScope scope, String name, IBroadcastStream stream);

	/**
	 * Get names of existing broadcast streams in a scope. 
	 * 
	 * @param scope         Scope to get stream names from
	 * @return              List of stream names
	 */
	List<String> getBroadcastStreamNames(IScope scope);

	/**
	 * Unregister a broadcast stream of a specific name from a scope.
	 * 
	 * @param scope         Scope
	 * @param name          Stream name
	 * @return <tt>true</tt> if unregister successfully.
	 */
	boolean unregisterBroadcastStream(IScope scope, String name);
	
	/**
	 * Unregister a broadcast stream of a specific name from a scope.
	 * 
	 * @param scope         Scope
	 * @param name          Stream name
	 * @param stream		Broadcast stream
	 * @return <tt>true</tt> if unregister successfully.
	 */
	boolean unregisterBroadcastStream(IScope scope, String name, IBroadcastStream stream);
	
}
