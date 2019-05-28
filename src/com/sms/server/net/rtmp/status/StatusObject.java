package com.sms.server.net.rtmp.status;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sms.annotations.Anonymous;
import com.sms.io.object.ICustomSerializable;
import com.sms.io.object.Output;
import com.sms.io.object.Serializer;

/**
 * Status object that is sent to client with every status event
 */
@Anonymous
public class StatusObject implements Serializable, ICustomSerializable, Externalizable {

	private static final long serialVersionUID = 8817297676191096283L;

	public static final String ERROR = "error";

	public static final String STATUS = "status";

	public static final String WARNING = "warning";

	protected String code;

	protected String level;

	protected String description = "";

	protected Object application;

	protected Map<String, Object> additional;
	
	/** Constructs a new StatusObject. */
    public StatusObject() {

	}

	public StatusObject(String code, String level, String description) {
		this.code = code;
		this.level = level;
		this.description = description;
	}

	/**
     * Getter for property 'code'.
     *
     * @return Value for property 'code'.
     */
    public String getCode() {
		return code;
	}

	/**
     * Setter for property 'code'.
     *
     * @param code Value to set for property 'code'.
     */
    public void setCode(String code) {
		this.code = code;
	}

	/**
     * Getter for property 'description'.
     *
     * @return Value for property 'description'.
     */
    public String getDescription() {
		return description;
	}

	/**
     * Setter for property 'description'.
     *
     * @param description Value to set for property 'description'.
     */
    public void setDescription(String description) {
		this.description = description;
	}

	/**
     * Getter for property 'level'.
     *
     * @return Value for property 'level'.
     */
    public String getLevel() {
		return level;
	}

	/**
     * Setter for property 'level'.
     *
     * @param level Value to set for property 'level'.
     */
    public void setLevel(String level) {
		this.level = level;
	}

	/**
     * Setter for property 'application'.
     *
     * @param application Value to set for property 'application'.
     */
    public void setApplication(Object application) {
		this.application = application;
	}

	/**
     * Getter for property 'application'.
     *
     * @return Value for property 'application'.
     */
    public Object getApplication() {
		return application;
	}

	/** {@inheritDoc} */
    @Override
	public String toString() {
		return String.format("Status code: %s level: %s description: %s", code, level, description);
	}

    /**
     * Generate Status object that can be returned through a RTMP channel.
     * 
     * @return status
     */ 
    public Status asStatus() {
    	return new Status(getCode(), getLevel(), getDescription());
    }

    public void setAdditional(String name, Object value) {
    	if ("code,level,description,application".indexOf(name) != -1) {
    		throw new RuntimeException("the name \"" + name + "\" is reserved");
    	}
    	if (additional == null) {
    		additional = new HashMap<String, Object>();
    	}
    	additional.put(name, value);
    }
    
    public void serialize(Output output) {
    	output.putString("level");
    	output.writeString(getLevel());
    	output.putString("code");
    	output.writeString(getCode());
    	output.putString("description");
    	output.writeString(getDescription());
    	if (application != null) {
    		output.putString("application");
    		Serializer.serialize(output, application);
    	}
    	if (additional != null) {
    		// Add additional parameters
    		for (Map.Entry<String, Object> entry: additional.entrySet()) {
    	    	output.putString(entry.getKey());
    	    	Serializer.serialize(output, entry.getValue());
    		}
    	}
    }

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		code = (String) in.readObject();
		description = (String) in.readObject();
		level = (String) in.readObject();
		additional = (Map<String, Object>) in.readObject();
		application = in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(code);
		out.writeObject(description);
		out.writeObject(level);
		if (application != null) {
			out.writeObject(additional);
		}
		if (additional != null) {
			out.writeObject(application);
		}
	}
}
