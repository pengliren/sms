package com.sms.server.api.stream;

import com.sms.server.api.IScope;

/**
 * Interface for handlers that control access to stream publishing.
 */
public interface IStreamPublishSecurity {

	/**
	 * Check if publishing a stream with the given name is allowed.
	 * 
	 * @param scope Scope the stream is about to be published in.
	 * @param name Name of the stream to publish.
	 * @param mode Publishing mode.
	 * @return <code>True</code> if publishing is allowed, otherwise <code>False</code>
	 */
	public boolean isPublishAllowed(IScope scope, String name, String mode);
	
}
