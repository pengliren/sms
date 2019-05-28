package com.sms.io.amf3;

/**
 * Interface that needs to be implemented by classes that serialize / deserialize
 * themselves.
 * 
 * @see <a href="http://livedocs.adobe.com/flex/2/langref/flash/utils/IExternalizable.html">Adobe Livedocs (external)</a>
 */
public interface IExternalizable {

	/**
	 * Load custom object from stream.
	 * 
	 * @param input object to be used for data loading
	 */
	public void readExternal(IDataInput input);
	
	/**
	 * Store custom object to stream.
	 * 
	 * @param output object to be used for data storing
	 */
	public void writeExternal(IDataOutput output);
	
}
