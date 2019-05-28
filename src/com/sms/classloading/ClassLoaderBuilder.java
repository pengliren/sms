package com.sms.classloading;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to get the Servlet Class loader. The class loader returned is a
 * child first class loader. 
 * 
 * <br />
 * <i>This class is based on original code from the XINS project, by 
 * Anthony Goubard (anthony.goubard@japplis.com)</i>
 */
public final class ClassLoaderBuilder {

	/*
	 http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6500212
	 http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6516909
	 http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4976356
	 */

	/**
	 * Filters jar files
	 */
	public final static class JarFileFilter implements FilenameFilter {
		/**
		 * Check whether file matches filter rules
		 * 
		 * @param dir Directory
		 * @param name File name
		 * @return true If file does match filter rules, false otherwise
		 */
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	}

	/**
	 * Gets a class loader based on mode.
	 * 
	 * @param path the directory or file containing classes
	 * 
	 * @param mode the mode in which the servlet should be loaded. The possible
	 *            values are <code>USE_CURRENT_CLASSPATH</code>,
	 *            <code>USE_CLASSPATH_LIB</code>, <code>USE_XINS_LIB</code>,
	 *            <code>USE_WAR_LIB</code>.
	 * 
	 * @param parent the parent class loader or null if you want the current threads
	 * 			class loader
	 * 
	 * @return the Class loader to use to load the required class(es).
	 * 
	 */
	@SuppressWarnings("unused")
	public static ClassLoader build() {

		JarFileFilter jarFileFilter = new JarFileFilter();

		List<URL> urlList = new ArrayList<URL>(31);

		//the class loader to return
		ClassLoader loader = null;

		//urls to load resources / classes from
		URL[] urls = null;

		String home = System.getProperty("sms.root");
		// if home is null check environmental
		if (home == null) {
			//check for env variable
			home = System.getenv("SMS_HOME");
		}
		//if home is null or equal to "current" directory
		if (home == null || ".".equals(home)) {
			//if home is still null look it up via this classes loader
			String classLocation = ClassLoaderBuilder.class.getProtectionDomain().getCodeSource().getLocation().toString();
			//System.out.printf("Classloader location: %s\n", classLocation);
			//snip off anything beyond the last slash
			home = classLocation.substring(0, classLocation.lastIndexOf('/'));
		}

		//get red5 lib system property, if not found build it	
		String libPath = System.getProperty("sms.lib_root");
		if (libPath == null) {
			//construct the lib path
			libPath = home + "/lib";
		}
		//System.out.printf("Library path: %s\n", libPath);	

		//grab the urls for all the jars in "lib"
		File libDir = new File(libPath);
		//if we are on osx with spaces in our path this may occur
		if (libDir == null) {
			libDir = new File(home, "lib");
		}
		File[] libFiles = libDir.listFiles(jarFileFilter);
		for (File lib : libFiles) {
			try {
				urlList.add(lib.toURI().toURL());
			} catch (MalformedURLException e) {
				System.err.printf("Exception %s\n", e);
			}
		}
		
		//get config dir
		String conf = System.getProperty("sms.config_root");
		if (conf == null) {
			conf = home + "/conf";
		}
		// add config dir
		try {
			URL confUrl = new File(conf).toURI().toURL();
			if (!urlList.contains(confUrl)) {
				urlList.add(confUrl);
			}
		} catch (MalformedURLException e) {
			System.err.printf("Exception %s\n", e);
		}

		//add the plugins 

		//get red5 lib system property, if not found build it	
		String pluginsPath = System.getProperty("sms.plugins_root");
		if (pluginsPath == null) {
			//construct the plugins path
			pluginsPath = home + "/plugins";
			//update the property
			System.setProperty("sms.plugins_root", pluginsPath);
		}
		// create the directory if it doesnt exist
		File pluginsDir = new File(pluginsPath);
		//if we are on osx with spaces in our path this may occur
		if (pluginsDir == null) {
			pluginsDir = new File(home, "plugins");
			//create the dir
			pluginsDir.mkdirs();
		}			
		// add the plugin directory to the path so that configs
		// will be resolved and not have to be copied to conf
		try {
			URL pluginsUrl = pluginsDir.toURI().toURL();
			if (!urlList.contains(pluginsUrl)) {
				urlList.add(pluginsUrl);
			}
		} catch (MalformedURLException e) {
			System.err.printf("Exception %s\n", e);
		}	
		//get all the plugin jars
		File[] pluginsFiles = pluginsDir.listFiles(jarFileFilter);
		//this can be null if the dir doesnt exist
		if (pluginsFiles != null) {
			for (File plugin : pluginsFiles) {
				try {
					urlList.add(plugin.toURI().toURL());
				} catch (MalformedURLException e) {
					System.err.printf("Exception %s\n", e);
				}
			}
		}

		//create the url array that the classloader wants
		urls = urlList.toArray(new URL[0]);
		//System.out.printf("Selected libraries: (%s items)\n", urls.length);
		//for (URL url : urls) {
		//	System.out.println(url);
		//}		
		//System.out.println();
		// instance a url classloader using the selected jars
		loader = new URLClassLoader(urls);

		Thread.currentThread().setContextClassLoader(loader);

		//loop thru all the current urls
		//System.out.printf("Classpath for %s:\n", loader);
		//for (URL url : urls) {
		//System.out.println(url.toExternalForm());
		//}

		return loader;
	}
}
