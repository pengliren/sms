package com.sms.io.m4a.impl;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.ITagReader;
import com.sms.io.ITagWriter;
import com.sms.io.m4a.IM4A;

/**
 * A M4AImpl implements the M4A api
 */
public class M4A implements IM4A {

	protected static Logger log = LoggerFactory.getLogger(M4A.class);

	private File file;

	/**
	 * Default constructor, used by Spring so that parameters may be injected.
	 */
	public M4A() {
	}

	/**
	 * Create M4A from given file source
	 * 
	 * @param file
	 *            File source
	 */
	public M4A(File file) {
		this.file = file;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITagReader getReader() throws IOException {
		return new M4AReader(file);
	}

	/**
	 * {@inheritDoc}
	 */
	public ITagWriter getWriter() throws IOException {
		return null;
	}

	public ITagWriter getAppendWriter() throws IOException {
		return null;
	}
	
}
