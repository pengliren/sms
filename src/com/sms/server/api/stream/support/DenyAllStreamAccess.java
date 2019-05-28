package com.sms.server.api.stream.support;

import com.sms.server.api.IScope;
import com.sms.server.api.stream.IStreamPlaybackSecurity;
import com.sms.server.api.stream.IStreamPublishSecurity;

/**
 * Stream security handler that denies access to all streams.
 */
public class DenyAllStreamAccess implements IStreamPublishSecurity,
		IStreamPlaybackSecurity {

	/** {@inheritDoc} */
	public boolean isPublishAllowed(IScope scope, String name, String mode) {
		return false;
	}

	/** {@inheritDoc} */
	public boolean isPlaybackAllowed(IScope scope, String name, int start,
			int length, boolean flushPlaylist) {
		return false;
	}

}
