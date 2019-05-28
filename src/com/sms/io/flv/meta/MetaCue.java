package com.sms.io.flv.meta;

import java.util.HashMap;
import java.util.Map;

/**
 * Cue point is metadata marker used to control and accompany video playback with client-side application
 * events. Each cue point have at least one attribute, timestamp. Timestamp specifies position of cue point in
 * FLV file.
 *
 * <p>Cue points are usually used as event triggers down video flow or navigation points in a file. Cue points are
 * of two types:
 * <ul>
 *  <li>Embedded into FLV or SWF</li>
 *  <li>External, or added on fly (e.g. with FLVPlayback component or ActionScript) on both server-side and client-side.</li>
 * </ul>
 * </p>
 *
 * <p>To add cue point trigger event listener at client-side in Flex/Flash application, use NetStream.onCuePoint event
 * handler.</p>
 *
 * @param <K> key type
 * @param <V> value type
 */
public class MetaCue<K, V> extends HashMap<String, Object> implements IMetaCue {

	/**
	 * SerialVersionUID = -1769771340654996861L;
	 */
	private static final long serialVersionUID = -1769771340654996861L;

	/**
	 * CuePoint constructor
	 */
	public MetaCue() {

	}

	/** {@inheritDoc}
	 */
	public void setName(String name) {
		this.put("name", name);
	}

	/** {@inheritDoc}
	 */
	public String getName() {
		return (String) this.get("name");
	}

	/** {@inheritDoc}
	 */
	public void setType(String type) {
		this.put("type", type);
	}

	/** {@inheritDoc}
	 */
	public String getType() {
		return (String) this.get("type");
	}

	/** {@inheritDoc}
	 */
	public void setTime(double d) {
		this.put("time", d);
	}

	/** {@inheritDoc}
	 */
	public double getTime() {
		return (Double) this.get("time");
	}

	/** {@inheritDoc} */
	public int compareTo(Object arg0) {
		MetaCue<?, ?> cp = (MetaCue<?, ?>) arg0;
		double cpTime = cp.getTime();
		double thisTime = this.getTime();

		if (cpTime > thisTime) {
			return -1;
		} else if (cpTime < thisTime) {
			return 1;
		}

		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MetaCue{");
		for (Map.Entry<String, Object> entry : entrySet()) {
			sb.append(entry.getKey().toLowerCase());
			sb.append('=');
			sb.append(entry.getValue());
		}
		sb.append('}');
		return sb.toString();
	}
}
