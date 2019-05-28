package com.sms.server.api;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;

import com.sms.server.util.SystemTimer;

/**
 * Utility class for accessing Red5 API objects.
 *
 * This class uses a thread local, and will be setup by the service invoker.
 *  
 * The code below shows various uses. 
 * <br />
 * <pre> 
 * IConnection conn = SMS.getConnectionLocal();
 * SMS sms = new SMS(); 
 * IScope scope = sms.getScope();
 * conn = sms.getConnection();
 * sms = new SMS(conn);
 * IClient client = sms.getClient();
 * </pre>
 * 
 */
public final class SMS {

	/**
	 * Current connection thread. Each connection of Red5 application runs in a
	 * separate thread. This is thread object associated with the current connection.
	 */
	private static ThreadLocal<WeakReference<IConnection>> connThreadLocal = new ThreadLocal<WeakReference<IConnection>>();

	/**
	 * Connection local to the current thread 
	 */
	public IConnection conn;

	/**
	 * Current server version with revision
	 */
	public static final String VERSION = "SMS-1.0 $";

	/**
	 * Current server version for fmsVer requests 
	 */
	public static final String FMS_VERSION = "ESC/1,0,0,0";

	/**
	 * Data version for NetStatusEvents
	 */
	@SuppressWarnings("serial")
	public static final Map<String, Object> DATA_VERSION = new HashMap<String, Object>(2) {
		{
			put("version", "1.0");
			put("type", "SMS");
		}
	};

	/**
	 * Server start time
	 */
	private static final long START_TIME = SystemTimer.currentTimeMillis();

	/**
	 * Create a new Red5 object using given connection.
	 * 
	 * @param conn Connection object.
	 */
	public SMS(IConnection conn) {
		this.conn = conn;
	}

	/**
	 * Create a new Red5 object using the connection local to the current thread
	 * A bit of magic that lets you access the red5 scope from anywhere
	 */
	public SMS() {
		conn = SMS.getConnectionLocal();
	}

	/**
	 * Setter for connection
	 *
	 * @param connection     Thread local connection
	 */
	public static void setConnectionLocal(IConnection connection) {
		if (connection != null) {
    		connThreadLocal.set(new WeakReference<IConnection>(connection));
//    		IScope scope = connection.getScope();
//    		if (scope != null) {
//    			Thread.currentThread().setContextClassLoader(scope.getClassLoader());
//    		}
		} else {
			// use null to clear the value
			connThreadLocal.remove();
		}
	}

	/**
	 * Get the connection associated with the current thread. This method allows
	 * you to get connection object local to current thread. When you need to
	 * get a connection associated with event handler and so forth, this method
	 * provides you with it.
	 * 
	 * @return Connection object
	 */
	public static IConnection getConnectionLocal() {
		WeakReference<IConnection> ref = connThreadLocal.get();
		if (ref != null) {
			return ref.get();
		} else {
			return null;
		}
	}

	/**
	 * Get the connection object.
	 * 
	 * @return Connection object
	 */
	public IConnection getConnection() {
		return conn;
	}

	/**
	 * Get the scope
	 * 
	 * @return Scope object
	 */
	public IScope getScope() {
		return conn.getScope();
	}

	/**
	 * Get the client
	 * 
	 * @return Client object
	 */
	public IClient getClient() {
		return conn.getClient();
	}

	/**
	 * Get the spring application context
	 * 
	 * @return Application context
	 */
	public IContext getContext() {
		return conn.getScope().getContext();
	}

	/**
	 * Returns the current version with revision number
	 * 
	 * @return String version
	 */
	public static String getVersion() {
		return VERSION;
	}

	/**
	 * Returns the current version for fmsVer requests
	 *
	 * @return String fms version
	 */
	public static String getFMSVersion() {
		return FMS_VERSION;
	}

	public static Object getDataVersion() {
		return DATA_VERSION;
	}

	/**
	 * Returns server uptime in milliseconds.
	 *
	 * @return String version
	 */
	public static long getUpTime() {
		return SystemTimer.currentTimeMillis() - START_TIME;
	}

	/**
	 * Allows for reconstruction via CompositeData.
	 * 
	 * @param cd composite data
	 * @return Red5 class instance
	 */
	public static SMS from(CompositeData cd) {
		SMS instance = null;
		if (cd.containsKey("connection")) {
			Object cn = cd.get("connection");
			if (cn != null && cn instanceof IConnection) {
				instance = new SMS((IConnection) cn);
			} else {
				instance = new SMS();
			}
		} else {
			instance = new SMS();
		}
		return instance;
	}

}
