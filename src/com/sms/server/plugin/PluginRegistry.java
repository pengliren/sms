package com.sms.server.plugin;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.plugin.IPlugin;

public class PluginRegistry {

	private static Logger log = LoggerFactory.getLogger(PluginRegistry.class);

	//keeps track of plug-ins, keyed by plug-in name
	private static volatile ConcurrentMap<String, IPlugin> plugins = new ConcurrentHashMap<String, IPlugin>();
	
	//locks for guarding plug-ins
	private final static ReadWriteLock pluginLock = new ReentrantReadWriteLock(); 
    private final static Lock pluginReadLock; 
    private final static Lock pluginWriteLock; 	
	
	static {
		pluginReadLock = pluginLock.readLock(); 
		pluginWriteLock = pluginLock.writeLock(); 
	}
    
	/**
	 * Registers a plug-in.
	 * 
	 * @param plugin
	 */
	public static void register(IPlugin plugin) {
		log.debug("Register plugin: {}", plugin);
		String pluginName = plugin.getName();
		//get a write lock
		pluginWriteLock.lock();
		try {
			if (plugins.containsKey(pluginName)) {
				//get old plugin
				IPlugin oldPlugin = plugins.get(pluginName);
				//if they are not the same shutdown the older one
				if (!plugin.equals(oldPlugin)) {			
					try {
						oldPlugin.doStop();
					} catch (Exception e) {
						log.warn("Exception caused when stopping old plugin", e);
					}
					//replace old one
					plugins.replace(pluginName, plugin);
				}
			} else {
				plugins.put(pluginName, plugin);
			}
		} finally {
			pluginWriteLock.unlock();
		}		
	}
	
	/**
	 * Unregisters a plug-in.
	 * 
	 * @param plugin
	 */
	public static void unregister(IPlugin plugin) {
		log.debug("Unregister plugin: {}", plugin);
		//get a write lock
		pluginWriteLock.lock();
		try {
			if (plugins.containsValue(plugin)) {
				boolean removed = false;
				for (Entry<String, IPlugin> f : plugins.entrySet()) {
					if (plugin.equals(f.getValue())) {
						log.debug("Removing {}", plugin);
						plugins.remove(f.getKey());
						removed = true;
						break;
					} else {
						log.debug("Not equal - {} {}", plugin, f.getValue());
					}
				}
				if (!removed) {
					log.debug("Last try to remove the plugin");
					plugins.remove(plugin.getName());
				}
			} else {
				log.warn("Plugin is not registered {}", plugin);
			}
		} finally {
			pluginWriteLock.unlock();
		}			
	}
	
	/**
	 * Returns a plug-in.
	 * 
	 * @param pluginName
	 * @return requested plug-in matching the name given or null if not found
	 */
	public static IPlugin getPlugin(String pluginName) {
		IPlugin plugin = null;
		pluginReadLock.lock();
		try {
			plugin = plugins.get(pluginName);
		} finally {
			pluginReadLock.unlock();
		}
		return plugin;
	}	
	
	/**
	 * Shuts down the registry and stops any plug-ins that are found.
	 * 
	 * @throws Exception
	 */
	public static void shutdown() throws Exception {
		log.info("Destroying and cleaning up {} plugins", plugins.size());	
		//loop through the plugins and stop them
		pluginReadLock.lock();
		try {
			for (Entry<String, IPlugin> plugin : plugins.entrySet()) {
				plugin.getValue().doStop();
			}
		} finally {
			pluginReadLock.unlock();
		}
		plugins.clear();
	}
}
