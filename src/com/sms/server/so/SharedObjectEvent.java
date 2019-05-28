package com.sms.server.so;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SharedObjectEvent implements ISharedObjectEvent, Externalizable {

	private static final long serialVersionUID = -4129018814289863535L;

	/**
	 * Event type
	 */
	private Type type;

	/**
	 * Changed pair key
	 */
	private String key;

	/**
	 * Changed pair value
	 */
	private Object value;

	public SharedObjectEvent() {
	}

	/**
	 * 
	 * @param type type
	 * @param key key
	 * @param value value
	 */
	public SharedObjectEvent(Type type, String key, Object value) {
		this.type = type;
		this.key = key;
		this.value = value;
	}

	/** {@inheritDoc} */
	public String getKey() {
		return key;
	}

	/** {@inheritDoc} */
	public Type getType() {
		return type;
	}

	/** {@inheritDoc} */
	public Object getValue() {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SOEvent(" + getType() + ", " + getKey() + ", " + getValue()
				+ ')';
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		type = (Type) in.readObject();
		key = (String) in.readObject();
		value = in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(type);
		out.writeObject(key);
		out.writeObject(value);
	}
}
