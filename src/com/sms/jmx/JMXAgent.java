package com.sms.jmx;

import java.io.File;
import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.Configuration;

public class JMXAgent implements NotificationListener {

	private static Logger log = LoggerFactory.getLogger(JMXAgent.class);
	
	private static JMXConnectorServer cs;

	private static MBeanServer mbs;

	private static String rmiAdapterPort = String.valueOf(Configuration.JMX_RMI_PORT_REGISTRY);

	private static String rmiAdapterRemotePort = Configuration.JMX_RMI_PORT_REMOTEOBJECTS;

	private static String rmiAdapterHost = Configuration.JMX_RMI_HOST;

	private static String remotePasswordProperties = "jmxremote.password";

	private static String remoteAccessProperties = "jmxremote.access";

	static {
		//in the war version the jmx factory is not created before
		//registration starts ?? so we check for it here and init
		//if needed
		if (null == mbs) {
			mbs = JMXFactory.getMBeanServer();
		}
	}

	/**
	 * Convenience to remove packages etc from a class name.
	 * 
	 * @param className class name to trim
	 * @return trimmed class name
	 */
	public static String trimClassName(String className) {
		if (StringUtils.isNotBlank(className)) {
			if (className.indexOf('.') != -1) {
				//strip package stuff
				className = className.substring(className.lastIndexOf('.') + 1);
			}
		}
		return className;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean registerMBean(Object instance, String className, Class interfaceClass) {
		boolean status = false;
		try {
			String cName = className;
			if (cName.indexOf('.') != -1) {
				cName = cName.substring(cName.lastIndexOf('.')).replaceFirst("[\\.]", "");
			}
			log.debug("Register name: {}", cName);
			mbs.registerMBean(new StandardMBean(instance, interfaceClass), new ObjectName(JMXFactory.getDefaultDomain() + ":type=" + cName));
			status = true;
		} catch (InstanceAlreadyExistsException iaee) {
			log.debug("Already registered: {}", className);
		} catch (Exception e) {
			log.error("Could not register the {} MBean. {}", className, e);
		}
		return status;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean registerMBean(Object instance, String className, Class interfaceClass, ObjectName name) {
		boolean status = false;
		try {
			String cName = className;
			if (cName.indexOf('.') != -1) {
				cName = cName.substring(cName.lastIndexOf('.')).replaceFirst("[\\.]", "");
			}
			log.debug("Register name: {}", cName);
			mbs.registerMBean(new StandardMBean(instance, interfaceClass), name);
			status = true;
		} catch (InstanceAlreadyExistsException iaee) {
			log.debug("Already registered: {}", className);
		} catch (Exception e) {
			log.error("Could not register the {} MBean. {}", className, e);
		}
		return status;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean registerMBean(Object instance, String className, Class interfaceClass, String name) {
		boolean status = false;
		try {
			String cName = className;
			if (cName.indexOf('.') != -1) {
				cName = cName.substring(cName.lastIndexOf('.')).replaceFirst("[\\.]", "");
			}
			log.debug("Register name: {}", cName);
			mbs.registerMBean(new StandardMBean(instance, interfaceClass), new ObjectName(JMXFactory.getDefaultDomain() + ":type=" + cName + ",name=" + name));
			status = true;
		} catch (InstanceAlreadyExistsException iaee) {
			log.debug("Already registered: {}", className);
		} catch (Exception e) {
			log.error("Could not register the {} MBean. {}", className, e);
		}
		return status;
	}

	/**
	 * Shuts down any instanced connectors.
	 */
	@SuppressWarnings("cast")
	public static void shutdown() {
		log.info("Shutting down JMX agent");
		if (null != cs) {
			try {
				//stop the connector
				cs.stop();
			} catch (Exception e) {
				log.error("Exception stopping JMXConnector server {}", e);
			}
		}
		try {
			//unregister all the currently registered red5 mbeans
			String domain = JMXFactory.getDefaultDomain();
			for (ObjectName oname : mbs.queryNames(new ObjectName(domain + ":*"), null)) {
				log.debug("Bean domain: {}", oname.getDomain());
				if (domain.equals(oname.getDomain())) {
					unregisterMBean(oname);
				}
			}
		} catch (Exception e) {
			log.error("Exception unregistering mbeans {}", e);
		}

	}

	/**
	 * Unregisters an mbean instance. If the instance is not found or if a failure occurs, false will be returned.
	 * @param oName bean instance
	 * @return true if success; false if instance not found or failure
	 */
	public static boolean unregisterMBean(ObjectName oName) {
		boolean unregistered = false;
		if (null != oName) {
			try {
				if (mbs.isRegistered(oName)) {
					log.debug("Mbean is registered");
					mbs.unregisterMBean(oName);
					//set flag based on registration status
					unregistered = !mbs.isRegistered(oName);
				} else {
					log.debug("Mbean is not currently registered");
				}
			} catch (Exception e) {
				log.warn("Exception unregistering mbean {}", e);
			}
		}
		log.debug("leaving unregisterMBean...");
		return unregistered;
	}

	/**
	 * Updates a named attribute of a registered mbean.
	 *
	 * @param oName object name
	 * @param key key
	 * @param value new value
	 * @return true if success; false othwerwise
	 */
	public static boolean updateMBeanAttribute(ObjectName oName, String key, int value) {
		boolean updated = false;
		if (null != oName) {
			try {
				// update the attribute
				if (mbs.isRegistered(oName)) {
					mbs.setAttribute(oName, new javax.management.Attribute("key", value));
					updated = true;
				}
			} catch (Exception e) {
				log.error("Exception updating mbean attribute", e);
			}
		}
		return updated;
	}

	/**
	 * Updates a named attribute of a registered mbean.
	 *
	 * @param oName object name
	 * @param key key
	 * @param value new value
	 * @return true if success; false otherwise
	 */
	public static boolean updateMBeanAttribute(ObjectName oName, String key, String value) {
		boolean updated = false;
		if (null != oName) {
			try {
				// update the attribute
				if (mbs.isRegistered(oName)) {
					mbs.setAttribute(oName, new javax.management.Attribute("key", value));
					updated = true;
				}
			} catch (Exception e) {
				log.error("Exception updating mbean attribute", e);
			}
		}
		return updated;
	}

	public void handleNotification(Notification notification, Object handback) {
		log.debug("handleNotification {}", notification.getMessage());
	}

	public void init() {
		//environmental var holder
		HashMap<String, Object> env = null;

		if (Configuration.JMX_RMI_ENABLE) {
			// Create an RMI connector server
			log.debug("Create an RMI connector server");
			// bind the rmi hostname for systems with nat and multiple binded addresses !
			System.setProperty("java.rmi.server.hostname", rmiAdapterHost);
			try {
				Registry r = null;
				try {
					//lookup the registry
					r = LocateRegistry.getRegistry(Integer.valueOf(rmiAdapterPort));
					//ensure we are not already registered with the registry
					for (String regName : r.list()) {
						if (regName.equals("StreamServer")) {
							//unbind connector from rmi registry
							r.unbind("StreamServer");
						}
					}
				} catch (RemoteException re) {
					log.info("RMI Registry server was not found on port " + rmiAdapterPort);
					//if we didnt find the registry and the user wants it created
					log.info("Starting an internal RMI registry");
					r = LocateRegistry.createRegistry(Integer.valueOf(rmiAdapterPort));
				}
				JMXServiceURL url = null;
				// Configure the remote objects exported port for firewalls !!
				if (StringUtils.isNotEmpty(rmiAdapterRemotePort)) {
					url = new JMXServiceURL("service:jmx:rmi://" + rmiAdapterHost + ":" + rmiAdapterRemotePort + "/jndi/rmi://" + rmiAdapterHost + ":" + rmiAdapterPort + "/StreamServer");
				} else {
					url = new JMXServiceURL("service:jmx:rmi://" + rmiAdapterHost + ":" + rmiAdapterPort + "/jndi/rmi://" + rmiAdapterHost + ":" + rmiAdapterPort + "/StreamServer");
				}
				log.info("JMXServiceUrl is: {}", url);				
				//if authentication is requested
				if (StringUtils.isNotBlank(remoteAccessProperties)) {
					//if ssl is not used this will be null
					if (null == env) {
						env = new HashMap<String, Object>();
					}
					//check the existance of the files
					//in the war version the full path is needed
					File file = new File(remoteAccessProperties);
					if (!file.exists() && remoteAccessProperties.indexOf(System.getProperty("sms.config_root")) != 0) {
						log.debug("Access file was not found on path, will prepend config_root");
						//pre-pend the system property set in war startup
						remoteAccessProperties = System.getProperty("sms.config_root") + '/' + remoteAccessProperties;
						remotePasswordProperties = System.getProperty("sms.config_root") + '/' + remotePasswordProperties;
					}
					//strip "file:" prefixing
					env.put("jmx.remote.x.access.file", remoteAccessProperties.replace("file:", ""));
					env.put("jmx.remote.x.password.file", remotePasswordProperties.replace("file:", ""));
				}
				// create the connector server
				cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
				// add a listener for shutdown
				cs.addNotificationListener(this, null, null);
				// Start the RMI connector server
				log.debug("Start the RMI connector server");
				cs.start();
				log.info("JMX RMI connector server successfully started");
			} catch (ConnectException e) {
				log.warn("Could not establish RMI connection to port " + rmiAdapterPort + ", please make sure \"rmiregistry\" is running and configured to listen on this port.");
			} catch (IOException e) {
				String errMsg = e.getMessage();
				if (errMsg.indexOf("NameAlreadyBoundException") != -1) {
					log.error("JMX connector already registered, you will need to restart your rmiregistry");
				} else {
					log.error("{}", e);
				}
			} catch (Exception e) {
				log.error("Error in setup of JMX subsystem (RMI connector)", e);
			}
		}
	} 

	public void setRemoteAccessProperties(String remoteAccessProperties) {
		JMXAgent.remoteAccessProperties = remoteAccessProperties;
	}

	public void setRemotePasswordProperties(String remotePasswordProperties) {
		JMXAgent.remotePasswordProperties = remotePasswordProperties;
	}

	public void setRmiAdapterRemotePort(String rmiAdapterRemotePort) {
		JMXAgent.rmiAdapterRemotePort = rmiAdapterRemotePort;
	}

	public void setRmiAdapterPort(String rmiAdapterPort) {
		JMXAgent.rmiAdapterPort = rmiAdapterPort;
	}

	public void setRmiAdapterHost(String rmiAdapterHost) {
		JMXAgent.rmiAdapterHost = rmiAdapterHost;
	}
}
