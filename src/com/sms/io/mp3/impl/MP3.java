package com.sms.io.mp3.impl;
import java.io.File;
import java.io.IOException;

import com.sms.io.ITagReader;
import com.sms.io.ITagWriter;
import com.sms.io.mp3.IMP3;


/**
 * Represents MP3 file
 */
public class MP3 implements IMP3 {
    /**
     * Actual file object
     */
	private File file;

    /**
     * Creates MP3 object using given file
     * @param file           File object to use
     */
    public MP3(File file) {
		this.file = file;
	}

	/** {@inheritDoc} */
    public ITagReader getReader() throws IOException {
		return new MP3Reader(file);
	}

	/** {@inheritDoc} */
    public ITagWriter getWriter() throws IOException {
		return null;
	}

	/** {@inheritDoc} */
    public ITagWriter getAppendWriter() throws IOException {
		return null;
	}

}
