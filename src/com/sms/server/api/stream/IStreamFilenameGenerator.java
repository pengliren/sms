package com.sms.server.api.stream;

import com.sms.server.api.IScope;
import com.sms.server.api.IScopeService;

/**
 * A class that can generate filenames for streams.
 */
public interface IStreamFilenameGenerator extends IScopeService {

	/** Name of the bean to setup a custom filename generator in an application. */
	public static String BEAN_NAME = "streamFilenameGenerator";

	/** Possible filename generation types. */
	public static enum GenerationType {
			PLAYBACK,
			RECORD
	};
	
	/**
	 * Generate a filename without an extension.
	 * 
	 * @param scope           Scope to use
	 * @param name            Stream name
     * @param type            Generation strategy (either playback or record)
     * @return                Full filename
	 */
	public String generateFilename(IScope scope, String name, GenerationType type);

	/**
	 * Generate a filename with an extension.
	 *
	 * @param scope           Scope to use
	 * @param name            Stream filename
	 * @param extension       Extension
     * @param type            Generation strategy (either playback or record)
	 * @return                Full filename with extension
     */
	public String generateFilename(IScope scope, String name, String extension, GenerationType type);

	/**
     * True if returned filename is an absolute path, else relative to application.
     * 
     * If relative to application, you need to use
     * <code>scope.getContext().getResources(fileName)[0].getFile()</code> to resolve
     * this to a file.
     * 
     * If absolute (ie returns true) simply use <code>new File(generateFilename(scope, name))</code>
     * 
     * @return true if an absolute path; else false
     */
    public boolean resolvesToAbsolutePath();
	
}
