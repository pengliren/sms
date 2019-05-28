package com.sms.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class JMXUtil {

	public static MBeanServer getMBeanServer() {

		return JMXFactory.getMBeanServer();
	}

	public static Object getMBeanValue(String param1, String param2) {
		Object localObject = null;
		try {
			ObjectName localObjectName = new ObjectName(param1);
			localObject = getMBeanServer().getAttribute(localObjectName, param2);
		} catch (Exception exception) {
			localObject = null;
		}
		return localObject;
	}
	
	public static long getMBeanValueLong(String param1, String param2) {
		long value = 0l;
		try {
			ObjectName localObjectName = new ObjectName(param1);
			value = ((Long) getMBeanServer().getAttribute(localObjectName, param2)).longValue();
		} catch (Exception localException) {
			value = -1L;
		}
		return value;
	}
}
