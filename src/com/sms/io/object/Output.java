package com.sms.io.object;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Document;

import com.sms.io.amf3.ByteArray;

/**
 * Output interface which defines contract methods to be implemented
 */
public interface Output {

	boolean supportsDataType(byte type);

	void putString(String string);

	// Basic Data Types
	/**
	 * Write number
	 * @param num       Number
	 */
	void writeNumber(Number num);

	/**
	 * Write boolean
	 * @param bol       Boolean
	 */
	void writeBoolean(Boolean bol);

	/**
	 * Write string
	 * @param string    String
	 */
	void writeString(String string);

	/**
	 * Write date
	 * @param date      Date
	 */
	void writeDate(Date date);

	void writeNull();

	/**
	 * Write array.
	 * @param array     	Array to write.
	 */
	void writeArray(Collection<?> array);

	/**
	 * Write array.
	 * @param array     	Array to write.
	 */
	void writeArray(Object[] array);

	/**
	 * Write primitive array.
	 * @param array     	Array to write.
	 */
	void writeArray(Object array);

	/**
	 * Write map.
	 *
	 * @param map			Map to write
	 */
	void writeMap(Map<Object, Object> map);

	/**
	 * Write array as map.
	 *
	 * @param array			Array to write
	 */
	void writeMap(Collection<?> array);

	/**
	 * Write object.
	 *
	 * @param object		Object to write
	 */
	void writeObject(Object object);

	/**
	 * Write map as object.
	 *
	 * @param map			Map to write
	 */
	void writeObject(Map<Object, Object> map);

	/**
	 * Write recordset.
	 *
	 * @param recordset		Recordset to write.
	 */
	void writeRecordSet(RecordSet recordset);

	/**
	 * Write XML object
	 * @param xml      XML document
	 */
	void writeXML(Document xml);

	/**
	 * Write ByteArray object (AMF3 only).
	 * 
	 * @param array		object to write
	 */
	void writeByteArray(ByteArray array);

	/**
	 * Write a Vector<int>.
	 * 
	 * @param vector
	 */
	void writeVectorInt(Vector<Integer> vector);

	/**
	 * Write a Vector<uint>.
	 * 
	 * @param vector
	 */
	void writeVectorUInt(Vector<Long> vector);

	/**
	 * Write a Vector<Number>.
	 * 
	 * @param vector
	 */
	void writeVectorNumber(Vector<Double> vector);

	/**
	 * Write a Vector<Object>.
	 * 
	 * @param vector
	 */
	void writeVectorObject(Vector<Object> vector);

	/**
	 * Write reference to complex data type
	 * @param obj   Referenced object
	 */
	void writeReference(Object obj);

	/**
	 * Whether object is custom
	 *
	 * @param custom           Object
	 * @return                 true if object is of user type, false otherwise
	 */
	boolean isCustom(Object custom);

	/**
	 * Write custom (user) object
	 * @param custom     Custom data type object
	 */
	void writeCustom(Object custom);

	/**
	 * Clear references
	 */
	void clearReferences();
}
