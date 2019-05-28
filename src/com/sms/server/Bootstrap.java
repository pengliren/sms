package com.sms.server;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.sms.classloading.ClassLoaderBuilder;

/**
 * 
 * @ClassName: Bootstrap
 * @Description: 服务器启动和加载
 * @author pengliren
 * 
 */
public class Bootstrap {

	
	public static void main(String[] args) {
		
		if(args.length > 0) {
			if(args[0].equals("start")) {
				startServer();
			} else if(args[0].equals("stop")) {
				stopServer();
			} else {
				System.out.println("args is invalid!");
				doWait();
			} 
		} else {
			System.out.println("args length is invalid!");
			doWait();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void startServer() {
		
		String root = getSrvRoot();
		String conf = getConfigurationRoot(root);
		try {
			//get current loader
			ClassLoader baseLoader = Thread.currentThread().getContextClassLoader();
			// build a ClassLoader
			ClassLoader loader = ClassLoaderBuilder.build();
			//set new loader as the loader for this thread
			Thread.currentThread().setContextClassLoader(loader);
			// create a new instance of this class using new classloader
			//setup logback configure
			Class JoranConfigurator = Class.forName("ch.qos.logback.classic.joran.JoranConfigurator",true,loader);
			Class LoggerFactory = Class.forName("org.slf4j.LoggerFactory",false,loader);
            Object configurator = JoranConfigurator.newInstance();
            Object loggerContext = LoggerFactory.getMethod("getILoggerFactory").invoke(null);
            Class.forName("ch.qos.logback.classic.LoggerContext",false,loader).getMethod("reset").invoke(loggerContext);
            
            JoranConfigurator
            	.getMethod("setContext", Class.forName("ch.qos.logback.core.Context",false,loader))
            	.invoke(configurator, loggerContext);
            JoranConfigurator.getMethod("doConfigure", File.class).invoke(configurator, new File(conf,"logback.xml"));
          
			Class configBoot = Class.forName("com.sms.server.Configuration", true, loader);
			Method configMethod = configBoot.getMethod("initSystemConfig", String.class);
			boolean isOk = (Boolean)configMethod.invoke(configBoot, new Object[]{root});
			
			if(!isOk) {
				System.out.println("start fail, check config, will exit!");
				doWait();
				System.exit(-1);
			}
			
			Object jmxBoot =  Class.forName("com.sms.jmx.JMXAgent", true, loader).newInstance();
			Method jmxInitMethod = jmxBoot.getClass().getMethod("init", (Class[]) null);
			jmxInitMethod.invoke(jmxBoot, (Object[]) null);
			
			Object appBoot =  Class.forName("com.sms.server.ServerAppLoader", true, loader).newInstance();
			Method appStartMethod = appBoot.getClass().getMethod("start", (Class[]) null);
			appStartMethod.invoke(appBoot, (Object[]) null);		

			Object rtmpBoot = Class.forName("com.sms.server.net.rtmp.RTMPMinaTransport", true, loader).newInstance();
			Method rtmpStartMethod = rtmpBoot.getClass().getMethod("start", (Class[]) null);
			rtmpStartMethod.invoke(rtmpBoot, (Object[]) null);	

			Object httpBoot = Class.forName("com.sms.server.net.http.HTTPMinaTransport", true, loader).newInstance();
			Method httpStartMethod = httpBoot.getClass().getMethod("start", (Class[]) null);
			httpStartMethod.invoke(httpBoot, (Object[]) null);	
			
			Object rtspBoot = Class.forName("com.sms.server.net.rtsp.RTSPMinaTransport", true, loader).newInstance();
			Method rtspStartMethod = rtspBoot.getClass().getMethod("start", (Class[]) null);
			rtspStartMethod.invoke(rtspBoot, (Object[]) null);	
			
			final Object pluginBoot = Class.forName("com.sms.server.plugin.PluginLauncher", true, loader).newInstance();
			Method pluginStartMethod = pluginBoot.getClass().getMethod("start", (Class[]) null);
			pluginStartMethod.invoke(pluginBoot, (Object[]) null);
							
			//not that it matters, but set it back to the original loader
			Thread.currentThread().setContextClassLoader(baseLoader);		
			
			//regitster shutdown thread notify plugins stop
			System.out.println("register shutdwon thread");
	        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println("shutting down!");
					Method pluginStartMethod;
					try {
						pluginStartMethod = pluginBoot.getClass().getMethod("destroy", (Class[]) null);
						pluginStartMethod.invoke(pluginBoot, (Object[]) null);
					} catch (Exception e) {
						System.out.println("shutting down excute plugins destroy fail!");
						System.exit(-1);
					} 						
				}
			}));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(String.format("start server fail %s, will exit!", e.getCause()));
			doWait();
			System.exit(-1);
		}
	}
	
	private static void stopServer() {
		
		Socket socket;
		try {
			socket = new Socket("127.0.0.1", 5080);
			OutputStream os = socket.getOutputStream();
	        StringBuffer sb = new StringBuffer();
	        String method = "GET";
	        String uri = "/shutdown";
	        String version = "HTTP/1.1";
	        String date = httpDate();
	        sb.append(method + " " + uri + " " + version + "\r\n");
	        sb.append("Host: 127.0.0.1:5080\r\n");
	        sb.append("Date: " + date + "\r\n");
	        sb.append("Content-Length: " + 0 + "\r\n");
	        sb.append("\r\n");
	        byte[] data = sb.toString().getBytes();
	        os.write(data);
	        os.flush();
	        os.close();
	        socket.close();
		} catch (Exception e) {
			System.out.println(String.format("stop server fail %s!", e.getCause()));
		} 		
	}
	
	private static void doWait() {
		try {
			Thread.currentThread();
			Thread.sleep(5000L);
		} catch (Exception e) {
		}
	}
	
	public static String httpDate() {
		
		SimpleDateFormat sdft = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
		sdft.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdft.format(new Date()) + "GMT";
	}
	
	private static String getConfigurationRoot(String root) {
		
		// look for config dir
		String conf = System.getProperty("sms.config_root");
		// if root is not null and conf is null then default it
		if (root != null && conf == null) {
			conf = root + "/conf";
		}
		//flip slashes only if windows based os
		if (File.separatorChar != '/') {
			conf = conf.replaceAll("\\\\", "/");
		}
		//set conf sysprop
		System.setProperty("sms.config_root", conf);
		return conf;
	}
	
	private static String getSrvRoot() {
		
		String root = System.getProperty("sms.root");
		// if root is null check environmental
		if (root == null) {
			//check for env variable
			root = System.getenv("SMS_HOME");
		}
		// if root is null find out current directory and use it as root
		if (root == null || ".".equals(root)) {
			root = System.getProperty("user.dir");
		}
		//if were on a windows based os flip the slashes
		if (File.separatorChar != '/') {
			root = root.replaceAll("\\\\", "/");
		}
		//drop last slash if exists
		if (root.charAt(root.length() - 1) == '/') {
			root = root.substring(0, root.length() - 1);
		}
		//set/reset property
		System.setProperty("sms.root", root);		
		return root;
	}
}
