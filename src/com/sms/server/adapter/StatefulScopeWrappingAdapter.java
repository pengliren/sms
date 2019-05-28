package com.sms.server.adapter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sms.server.api.IAttributeStore;
import com.sms.server.api.IClient;
import com.sms.server.api.IConnection;
import com.sms.server.api.IContext;
import com.sms.server.api.IScope;
import com.sms.server.api.IScopeAware;
import com.sms.server.plugin.PluginDescriptor;

public class StatefulScopeWrappingAdapter extends AbstractScopeAdapter implements IScopeAware, IAttributeStore {

	/**
     * Wrapped scope
     */
    protected IScope scope;

    /**
	 * List of plug-in descriptors
	 */
	protected List<PluginDescriptor> plugins;

	/** {@inheritDoc} */
    public void setScope(IScope scope) {
		this.scope = scope;
	}

	/**
     * Getter for wrapped scope
     *
     * @return  Wrapped scope
     */
    public IScope getScope() {
		return scope;
	}

    /**
     * Returns any plug-ins descriptors added
     *     
     * @return plug-in descriptor list
     */
	public List<PluginDescriptor> getPlugins() {
		return plugins;
	}

	/**
	 * Adds a list of plug-in descriptors
	 * 
	 * @param plugins
	 */
	public void setPlugins(List<PluginDescriptor> plugins) {
		this.plugins = plugins;
	}

	/** {@inheritDoc} */
    public Object getAttribute(String name) {
		return scope.getAttribute(name);
	}

	/** {@inheritDoc} */
    public Object getAttribute(String name, Object defaultValue) {
		return scope.getAttribute(name, defaultValue);
	}

	/** {@inheritDoc} */
    public Set<String> getAttributeNames() {
		return scope.getAttributeNames();
	}

    /**
     * Wrapper for Scope#getAttributes
     * @return       Scope attributes map
     */
    public Map<String, Object> getAttributes() {
        return scope.getAttributes();
    }

    /** {@inheritDoc} */
    public boolean hasAttribute(String name) {
		return scope.hasAttribute(name);
	}

	/** {@inheritDoc} */
    public boolean removeAttribute(String name) {
		return scope.removeAttribute(name);
	}

	/** {@inheritDoc} */
    public void removeAttributes() {
		scope.removeAttributes();
	}

	/** {@inheritDoc} */
    public boolean setAttribute(String name, Object value) {
		return scope.setAttribute(name, value);
	}
    
    /** {@inheritDoc} */
	public int size() {
		return scope != null ? scope.getAttributeNames().size() : 0;
	}

	/** {@inheritDoc} */
	public boolean setAttributes(IAttributeStore attributes) {
		int successes = 0;
		for (Map.Entry<String, Object> entry : attributes.getAttributes().entrySet()) {
			if (scope.setAttribute(entry.getKey(), entry.getValue())) {
				successes++;
			}
		}		
		// expect every value to have been added
		return (successes == attributes.size());
	}

	/** {@inheritDoc} */
	public boolean setAttributes(Map<String, Object> attributes) {
		int successes = 0;
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			if (scope.setAttribute(entry.getKey(), entry.getValue())) {
				successes++;
			}
		}		
		// expect every value to have been added
		return (successes == attributes.size());
	}

    /**
     * Creates child scope
     * @param name        Child scope name
     * @return            <code>true</code> on success, <code>false</code> otherwise
     */
    public boolean createChildScope(String name) {
		return scope.createChildScope(name);
	}

    /**
     * Return child scope
     * @param name        Child scope name
     * @return            Child scope with given name
     */
    public IScope getChildScope(String name) {
		return scope.getScope(name);
	}

	/**
     * Iterator for child scope names
     *
     * @return  Iterator for child scope names
     */
    public Iterator<String> getChildScopeNames() {
		return scope.getScopeNames();
	}

	/**
     * Getter for set of clients
     *
     * @return  Set of clients
     */
    public Set<IClient> getClients() {
		return scope.getClients();
	}

	/**
     * Returns all connections in the scope
     *
     * @return  Connections
     */
    public Collection<Set<IConnection>> getConnections() {
		return scope.getConnections();
	}


    /**
     * Getter for context
     *
     * @return Value for context
     */
    public IContext getContext() {
		return scope.getContext();
	}

	/**
     * Getter for depth
     *
     * @return Value for depth
     */
    public int getDepth() {
		return scope.getDepth();
	}

	/**
     * Getter for name
     *
     * @return Value for name
     */
    public String getName() {
		return scope.getName();
	}

	/**
     * Return  parent scope
     *
     * @return  Parent scope
     */
    public IScope getParent() {
		return scope.getParent();
	}

	/**
     * Getter for stateful scope path
     *
     * @return Value for path
     */
    public String getPath() {
		return scope.getPath();
	}

    /**
     * Whether this scope has a child scope with given name
     * @param name       Child scope name
     * @return           <code>true</code> if it does have it, <code>false</code> otherwise
     */
    public boolean hasChildScope(String name) {
		return scope.hasChildScope(name);
	}

    /**
     * If this scope has a parent
     * @return            <code>true</code> if this scope has a parent scope, <code>false</code> otherwise
     */
    public boolean hasParent() {
		return scope.hasParent();
	}
	
	public Set<IConnection> lookupConnections(IClient client) {
		return scope.lookupConnections(client);
	}
}
