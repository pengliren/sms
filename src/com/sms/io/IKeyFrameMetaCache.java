package com.sms.io;

import java.io.File;

import com.sms.io.flv.IKeyFrameDataAnalyzer.KeyFrameMeta;

/**
 * Interface defining a cache for keyframe metadata informations.
 * 
 */
public interface IKeyFrameMetaCache {

	/**
	 * Load keyframe informations for the given file.
	 * 
	 * @param file		File to load informations for.
	 * @return The keyframe informations or <code>null</code> if none exist.
	 */
	public KeyFrameMeta loadKeyFrameMeta(File file);
	
	/**
	 * Store keyframe informations for the given file.
	 * 
	 * @param file		File to save informations for.
	 * @param meta		Keyframe informations for this file.
	 */
	public void saveKeyFrameMeta(File file, KeyFrameMeta meta);
	
}
