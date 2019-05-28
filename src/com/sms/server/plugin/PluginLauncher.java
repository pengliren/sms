package com.sms.server.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.Server;
import com.sms.server.api.plugin.IPlugin;

public class PluginLauncher {

	protected static Logger log = LoggerFactory.getLogger(PluginLauncher.class);
		
	public PluginLauncher() {
		
		
	}
	
	public void start() {
		
		Server server = Server.getInstance();

		//server should be up and running at this point so load any plug-ins now			

		//get the plugins dir
		File pluginsDir = new File(System.getProperty("sms.root"), "plugins");

		File[] plugins = pluginsDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				//lower the case
				String tmp = name.toLowerCase();
				//accept jars and zips
				return tmp.endsWith(".jar") || tmp.endsWith(".zip");
			}
		});
		
		if (plugins != null) {

			IPlugin red5Plugin = null;

			log.debug("{} plugins to launch", plugins.length);
			for (File plugin : plugins) {
    			JarFile jar;
    			Manifest manifest = null;
				try {
					jar = new JarFile(plugin, false);
					 manifest = jar.getManifest();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
    			
    			if (manifest == null) {
    				continue;
    			}
    			Attributes attributes = manifest.getMainAttributes();
    			if (attributes == null) {
    				continue;
    			}
    			String pluginMainClass = attributes.getValue("SMS-Plugin-Main-Class");
    			if (pluginMainClass == null || pluginMainClass.length() <= 0) {
    				continue;
    			}
    			// attempt to load the class; since it's in the plugins directory this should work
    			//ClassLoader loader = common.getClassLoader();
    			Class<?> pluginClass;
    			String pluginMainMethod = null;
    			try {
    				pluginClass = Class.forName(pluginMainClass);
    			} catch (ClassNotFoundException e) {
    				continue;
    			}
    			try {
					//handle plug-ins without "main" methods
					pluginMainMethod = attributes.getValue("SMS-Plugin-Main-Method");
					if (pluginMainMethod == null || pluginMainMethod.length() <= 0) {
						//just get an instance of the class
						red5Plugin = (IPlugin) pluginClass.newInstance();    				
					} else {
						Method method = pluginClass.getMethod(pluginMainMethod, (Class<?>[]) null);
						Object o = method.invoke(null, (Object[]) null);
						if (o != null && o instanceof IPlugin) {
							red5Plugin = (IPlugin) o;
						}
					}
					//register and start
					if (red5Plugin != null) {
						//set top-level context
						//red5Plugin.setApplicationContext(applicationContext);
						//set server reference
						red5Plugin.setServer(server);
						//register the plug-in to make it available for lookups
						PluginRegistry.register(red5Plugin);
						//start the plugin
						red5Plugin.doStart();
					}
					log.info("Loaded plugin: {}", pluginMainClass);
				} catch (Throwable t) {
					log.warn("Error loading plugin: {}; Method: {}", pluginMainClass, pluginMainMethod);
					log.error("", t);
				}
    		}
		} else {
			log.info("Plugins directory cannot be accessed or doesnt exist");
		}
	}
	
	public void destroy() throws Exception {
		PluginRegistry.shutdown();
	}
}
