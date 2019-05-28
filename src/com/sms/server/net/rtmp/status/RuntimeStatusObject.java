package com.sms.server.net.rtmp.status;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Runtime status object
 */
public class RuntimeStatusObject extends StatusObject {

	/**
	 * Serializable
	 */
	private static final long serialVersionUID = 6990998992583246039L;
    /**
     * Status event details
     */
	protected String details = "";
    /**
     * Client id
     */
	protected int clientid = 0;

	/** Constructs a new RuntimeStatusObject. */
    public RuntimeStatusObject() {
		super();
	}

    /**
     * Create runtime status object with given code, level and description
     * @param code                  Status code
     * @param level                 Level
     * @param description           Status event description
     */
	public RuntimeStatusObject(String code, String level, String description) {
		super(code, level, description);
	}

    /**
     * Create runtime status object with given code, level, description, details and client id
     * @param code                  Status code
     * @param level                 Level
     * @param description           Status event description
     * @param details               Status event details
     * @param clientid              Client id
     */
	public RuntimeStatusObject(String code, String level, String description,
			String details, int clientid) {
		super(code, level, description);
		this.details = details;
		this.clientid = clientid;
	}

	/**
     * Getter for client id
     *
     * @return  Client id
     */
    public int getClientid() {
		return clientid;
	}

	/**
     * Setter for client id
     *
     * @param clientid  Client id
     */
    public void setClientid(int clientid) {
		this.clientid = clientid;
	}

	/**
     * Getter for details
     *
     * @return  Status event details
     */
    public String getDetails() {
		return details;
	}

	/**
     * Setter for details
     *
     * @param details Status event details
     */
    public void setDetails(String details) {
		this.details = details;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		clientid = in.readInt();
		details = (String) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(clientid);
		out.writeObject(details);
	}
}
