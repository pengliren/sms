package com.sms.server;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IClient;
import com.sms.server.api.IConnection;
import com.sms.server.api.IScope;
import com.sms.server.api.SMS;
import com.sms.server.api.persistence.IPersistable;
import com.sms.server.stream.bandwidth.ClientServerDetection;
import com.sms.server.stream.bandwidth.ServerClientDetection;
import com.sms.server.util.SystemTimer;

public class Client extends AttributeStore implements IClient {

	/**
	 *  Logger
	 */
	protected static Logger log = LoggerFactory.getLogger(Client.class);

	/**
	 * Name of connection attribute holding the permissions.
	 */
	protected static final String PERMISSIONS = IPersistable.TRANSIENT_PREFIX + "_red5_permissions";

	/**
	 *  Scopes this client connected to
	 */
	protected ConcurrentMap<IConnection, IScope> connToScope = new ConcurrentHashMap<IConnection, IScope>();

	/**
	 *  Creation time as Timestamp
	 */
	protected long creationTime;

	/**
	 *  Clients identifier
	 */
	protected String id;

	/**
	 *  Client registry where Client is registered
	 */
	protected ClientRegistry registry;

	/**
	 * Whether or not the bandwidth has been checked.
	 */
	protected boolean bandwidthChecked;
	
	/**
	 * Creates client, sets creation time and registers it in ClientRegistry
	 *
	 * @param id             Client id
	 * @param registry       ClientRegistry
	 */
	@ConstructorProperties({"id", "registry"})
	public Client(String id, ClientRegistry registry) {
		this.id = id;
		this.registry = registry;
		this.creationTime = SystemTimer.currentTimeMillis();
	}

	/**
	 *  Disconnects client from Red5 application
	 */
	public void disconnect() {
		log.debug("Disconnect - id: {}, closing {} connections", id, getConnections().size());
		// Close all associated connections
		for (IConnection con : getConnections()) {
			con.close();
		}
		connToScope.clear();
	}

	/**
	 * Check clients equality by id
	 *
	 * @param obj        Object to check against
	 * @return           true if clients ids are the same, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Client) {
			return ((Client) obj).getId().equals(id);
		}
		return false;
	}

	/**
	 * Return set of connections for this client
	 *
	 * @return           Set of connections
	 */
	public Set<IConnection> getConnections() {
		return connToScope.keySet();
	}

	/**
	 * Return client connections to given scope
	 *
	 * @param scope           Scope
	 * @return                Set of connections for that scope
	 */
	public Set<IConnection> getConnections(IScope scope) {
		if (scope == null) {
			return getConnections();
		}
		Set<IConnection> result = new HashSet<IConnection>(connToScope.size());
		for (Entry<IConnection, IScope> entry : connToScope.entrySet()) {
			if (scope.equals(entry.getValue())) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	
	/**
	 *
	 * @return creation time
	 */
	public long getCreationTime() {
		return creationTime;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the client id
	 * @return client id
	 */
	public String getId() {
		return id;
	}

	/**
	 *
	 * @return scopes on this client
	 */
	public Collection<IScope> getScopes() {
		return connToScope.values();
	}

	/**
	 * if overriding equals then also do hashCode
	 * @return a has code
	 */
	@Override
	public int hashCode() {
		return Integer.valueOf(id);
	}

	/**
	 * Iterate through the scopes and their attributes.
	 * Used by JMX
	 *
	 * @return list of scope attributes
	 */
	public List<String> iterateScopeNameList() {
		log.debug("iterateScopeNameList called");
		int scopeCount = connToScope.values().size();
		List<String> scopeNames = new ArrayList<String>(scopeCount);
		log.debug("Scopes: {}", scopeCount);
		for (IScope scope : connToScope.values()) {
			log.debug("Client scope: {}", scope);
			for (Map.Entry<String, Object> entry : scope.getAttributes().entrySet()) {
				log.debug("Client scope attr: {} = {}", entry.getKey(), entry.getValue());
			}
		}
		return scopeNames;
	}

	/**
	 * Associate connection with client
	 * @param conn         Connection object
	 */
	protected void register(IConnection conn) {
		log.debug("Registering connection for this client {}", id);
		if (conn != null) {
			IScope scp =  conn.getScope();
			if (scp != null) {
				connToScope.put(conn, scp);
			} else {
				log.warn("Clients scope is null. Id: {}", id);
			}
		} else {
			log.warn("Clients connection is null. Id: {}", id);
		}
	}

	/**
	 *
	 * @return string representation of client
	 */
	@Override
	public String toString() {
		return "Client: " + id;
	}

	/**
	 * Removes client-connection association for given connection
	 * @param conn         Connection object
	 */
	protected void unregister(IConnection conn) {
		// Remove connection from connected scopes list
		connToScope.remove(conn);
		// If client is not connected to any scope any longer then remove
		if (connToScope.isEmpty()) {
			if (registry != null) {
				// This client is not connected to any scopes, remove from registry.
				registry.removeClient(this);
				registry = null;
			}
		}
	}
	
	/** {@inheritDoc} */
	public boolean isBandwidthChecked() {
		return bandwidthChecked;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	public Collection<String> getPermissions(IConnection conn) {
		Collection<String> result = (Collection<String>) conn.getAttribute(PERMISSIONS);
		if (result == null) {
			result = Collections.emptySet();
		}
		return result;
	}

	/** {@inheritDoc} */
	public boolean hasPermission(IConnection conn, String permissionName) {
		final Collection<String> permissions = getPermissions(conn);
		return permissions.contains(permissionName);
	}

	/** {@inheritDoc} */
	public void setPermissions(IConnection conn, Collection<String> permissions) {
		if (permissions == null) {
			conn.removeAttribute(PERMISSIONS);
		} else {
			conn.setAttribute(PERMISSIONS, permissions);
		}
	}
	
	/** {@inheritDoc} */
	public void checkBandwidth() {
		log.debug("Check bandwidth");
		bandwidthChecked = true;
		//do something to check the bandwidth, Dan what do you think?
		ServerClientDetection detection = new ServerClientDetection();
		detection.checkBandwidth(SMS.getConnectionLocal());
	}
	
	/** {@inheritDoc} */
	public Map<String, Object> checkBandwidthUp(Object[] params) {
		log.debug("Check bandwidth: {}", Arrays.toString(params));
		bandwidthChecked = true;
		//do something to check the bandwidth, Dan what do you think?
		ClientServerDetection detection = new ClientServerDetection();
		// if dynamic bw is turned on, we switch to a higher or lower
		return detection.checkBandwidth(params);
	}
	
	/**
	 * Allows for reconstruction via CompositeData.
	 * 
	 * @param cd composite data
	 * @return Client class instance
	 */
    public static Client from(CompositeData cd) {
    	Client instance = null;
    	if (cd.containsKey("id")) {
    		String id = (String) cd.get("id");
    		instance = new Client(id, null);
    		instance.setCreationTime((Long) cd.get("creationTime"));
    		instance.setAttribute(PERMISSIONS, cd.get(PERMISSIONS));
		}
        return instance;
    }	
}
