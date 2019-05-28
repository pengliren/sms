package com.sms.io;


import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for streamable file services.
 */
public abstract class BaseStreamableFileService implements
		IStreamableFileService {

    private static final Logger log = LoggerFactory.getLogger(BaseStreamableFileService.class);
	
	/** {@inheritDoc} */
    public void setPrefix(String prefix) {
    }

	/** {@inheritDoc} */
    public abstract String getPrefix();

	/** {@inheritDoc} */
    public void setExtension(String extension) {
    }
    
    /** {@inheritDoc} */
    public abstract String getExtension();

	/** {@inheritDoc} */
    public String prepareFilename(String name) {
    	String prefix = getPrefix() + ':';
		if (name.startsWith(prefix)) {
			name = name.substring(prefix.length());
			// if there is no extension on the file add the first one
			log.debug("prepareFilename - lastIndexOf: {} length: {}", name.lastIndexOf('.'), name.length());
			if (name.lastIndexOf('.') != name.length() - 4) {
				name = name + getExtension().split(",")[0];
			}
		}

		return name;
	}

	/** {@inheritDoc} */
    public boolean canHandle(File file) {
    	boolean valid = false;
    	if (file.exists()) {
        	String absPath = file.getAbsolutePath().toLowerCase();
        	String fileExt = absPath.substring(absPath.lastIndexOf('.'));
        	log.debug("canHandle - Path: {} Ext: {}", absPath, fileExt);
        	String[] exts = getExtension().split(",");
        	for (String ext : exts) {
        		if (ext.equals(fileExt)) {
        			valid = true;
        			break;
        		}
        	}
    	}
		return valid;
	}

	/** {@inheritDoc} */
    public abstract IStreamableFile getStreamableFile(File file)
			throws IOException;

}
