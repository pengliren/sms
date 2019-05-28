
package com.sms.server.stream;


import com.sms.server.api.IScope;
import com.sms.server.api.ScopeUtils;
import com.sms.server.api.stream.IStreamFilenameGenerator;

/**
 * Default filename generator for streams. The files will be stored in a
 * directory "streams" in the application folder. Option for changing directory
 * streams are saved to is investigated as of 0.6RC1.
 */
public class DefaultStreamFilenameGenerator implements IStreamFilenameGenerator {

	private static final class SingletonHolder {

		private static final DefaultStreamFilenameGenerator INSTANCE = new DefaultStreamFilenameGenerator();
	}

	public static DefaultStreamFilenameGenerator getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
    /**
     * Generate stream directory based on relative scope path. The base directory is
     * <code>streams</code>, e.g. a scope <code>/application/one/two/</code> will
     * generate a directory <code>/streams/one/two/</code> inside the application.
     * 
     * @param scope            Scope
     * @return                 Directory based on relative scope path
     */
    private String getStreamDirectory(IScope scope) {
		final StringBuilder result = new StringBuilder();
		final IScope app = ScopeUtils.findApplication(scope);
		final String prefix = "streams/";
		while (scope != null && scope != app) {
			result.insert(0, scope.getName() + "/");
			scope = scope.getParent();
		}
		if (result.length() == 0) {
			return prefix;
		} else {
			result.insert(0, prefix).append('/');
			return result.toString();
		}
    }

	/** {@inheritDoc} */
    public String generateFilename(IScope scope, String name, GenerationType type) {
		return generateFilename(scope, name, null, type);
	}

	/** {@inheritDoc} */
    public String generateFilename(IScope scope, String name, String extension, GenerationType type) {
		String result = getStreamDirectory(scope) + name;
		if (extension != null && !extension.equals("")) {
			result += extension;
		}
		return result;
	}

    /**
     * The default filenames are relative to the scope path, so always return <code>false</code>.
     * 
     * @return	always <code>false</code>
     */
	public boolean resolvesToAbsolutePath() {
		return false;
	}

}