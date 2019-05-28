package com.sms.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sms.jmx.JMXUtil;
import com.sms.server.util.FileUtil;

/**
 * 
 * @ClassName: Configuration
 * @Description: 读取配置信息
 * @author pengliren
 * 
 */
public final class Configuration {

	private static Logger log = LoggerFactory.getLogger(Configuration.class);
	
	public static String HTTP_HOST = "0.0.0.0";
	public static int HTTP_PORT = 5080;
	public static int HTTP_IO_THREADS = 2;
	public static int HTTP_WORKER_THREADS = 10;
	public static int HTTP_SEND_BUFFER_SIZE = 65536;
	public static int HTTP_RECEIVE_BUFFER_SIZE = 65536;
	public static boolean HTTP_TCP_NODELAY = true;
	public static int HTTP_MAX_BACKLOG = 5000;
	public static int HTTP_IDLE = 30;
	
	public static int HLS_SEGMENT_MAX = 3;
	public static int HLS_SEGMENT_TIME = 10;
	public static boolean HLS_ENCRYPT = false;

	public static String RTMP_HOST = "0.0.0.0";
	public static int RTMP_PORT = 1935;
	public static int RTMP_IO_THREADS = 2;
	public static int RTMP_WORKER_THREADS = 10;
	public static int RTMP_SEND_BUFFER_SIZE = 271360;
	public static int RTMP_RECEIVE_BUFFER_SIZE = 65536;
	public static int RTMP_PING_INTERVAL = 1000;
	public static int RTMP_MAX_INACTIVITY = 60000;
	public static int RTMP_MAX_HANDSHAKE_TIMEOUT = 5000;
	public static boolean RTMP_TCP_NODELAY = true;
	public static int RTMP_MAX_BACKLOG = 5000;
	public static int RTMP_DEFAULT_SERVER_BANDWIDTH = 10000000;
	public static int RTMP_DEFAULT_CLIENT_BANDWIDTH = 10000000;
	public static int RTMP_CLIENT_BANDWIDTH_LIMIT_TYPE = 2;
	public static boolean RTMP_BANDWIDTH_DETECTION = true;
	
	public static String RTSP_HOST = "0.0.0.0";
	public static int RTSP_PORT = 554;
	public static int RTSP_IO_THREADS = 2;
	public static int RTSP_WORKER_THREADS = 10;
	public static int RTSP_SEND_BUFFER_SIZE = 65536;
	public static int RTSP_RECEIVE_BUFFER_SIZE = 65536;
	public static boolean RTSP_TCP_NODELAY = true;
	public static int RTSP_MAX_BACKLOG = 5000;
	public static int UDP_PORT_START = 6970;
	
	public static String JMX_RMI_HOST = "0.0.0.0";
	public static String JMX_RMI_PORT_REMOTEOBJECTS = "";
	public static int JMX_RMI_PORT_REGISTRY = 9999;
	public static boolean JMX_RMI_ENABLE = false;
	
	public static long NOTIFY_SYSTIMER_TICK = 20;
	
	public static int FILECACHE_MAXSIZE = 500;
	public static int FILECACHE_PURGE = 10;
	public static int CACHE_INTERVAL = 10;
	
	
	public static int MULTICAST_EXECUTOR_THREADS = 4;
	public static int UNICAST_EXECUTOR_THREADS = 4;
	
	public static String MGR_CONN_URL = "";
	public static String MGR_SHARE_DIR = "";
	public static String MGR_HASP_VENDORCODE = "";
	public static long MGR_HASP_FEATUREID = 0;
	
	public static Map<String, ScopeContextBean> appConfigMap = new ConcurrentHashMap<String, ScopeContextBean>(); 
	private static Properties prop;
	
	public static boolean initSystemConfig(String path) {
		
		initSystemInfo();
		
		if(read(path) && readApp(path))
			return true;
		else
			return false;
	}
	
	private static void initSystemInfo() {
		
		Long totalPhysicalMemorySize = JMXUtil.getMBeanValueLong("java.lang:type=OperatingSystem", "TotalPhysicalMemorySize");
		Long totalSwapSpaceSize = JMXUtil.getMBeanValueLong("java.lang:type=OperatingSystem", "TotalSwapSpaceSize");
		Long freePhysicalMemorySize = JMXUtil.getMBeanValueLong("java.lang:type=OperatingSystem", "FreePhysicalMemorySize");
		Long freeSwapSpaceSize = JMXUtil.getMBeanValueLong("java.lang:type=OperatingSystem", "FreeSwapSpaceSize");
		
		if ((totalPhysicalMemorySize > 0L) && (freePhysicalMemorySize > 0L)) {
			log.info("Hardware Physical Memory: {}",  freePhysicalMemorySize / 1048576L + "MB" + "/" + totalPhysicalMemorySize / 1048576L + "MB");
		}
		
		if ((totalSwapSpaceSize > 0L) && (freeSwapSpaceSize > 0L)) {
			log.info("Hardware Swap Space: {}",  freeSwapSpaceSize / 1048576L + "MB" + "/" + totalSwapSpaceSize / 1048576L + "MB");
		}
		
		long maxFileDescriptorCount = JMXUtil.getMBeanValueLong("java.lang:type=OperatingSystem", "MaxFileDescriptorCount");
        log.info("Max File Descriptor Count: {}", (maxFileDescriptorCount <= 0L ? "Unlimited" : Long.valueOf(maxFileDescriptorCount)));
		if (maxFileDescriptorCount > 0L) {
			long openFileDescriptorCount = JMXUtil.getMBeanValueLong("java.lang:type=OperatingSystem", "OpenFileDescriptorCount");
			log.info("Open File Descriptor Count: {}", openFileDescriptorCount);
		}

		if (System.getProperty("sun.cpu.isalist") != null) {
			log.info("OS CPU: {}", System.getProperty("sun.cpu.isalist"));
		}
		log.info("OS CPU Available Processors: {}",  Runtime.getRuntime().availableProcessors());
		log.info("OS Name: {}", System.getProperty("os.name"));
        log.info("OS Arch: {}", System.getProperty("os.arch"));
        log.info("OS Version: {}", System.getProperty("os.version"));
        
		log.info("Java Name: {}", System.getProperty("java.vm.name"));
	    log.info("Java Vendor: {}", System.getProperty("java.vm.vendor"));
        log.info("Java Version: {}", System.getProperty("java.version"));
        log.info("Java VM Version: {}", System.getProperty("java.vm.version"));
        log.info("Java Spec Version: {}", System.getProperty("java.specification.version"));
        log.info("Java Home: {}", System.getProperty("java.home"));
        log.info("Java Max Heap Size: {}", Runtime.getRuntime().maxMemory() / 1048576L + "MB");
        log.info("Java Architecture: {}", System.getProperty("sun.arch.data.model"));
        log.info("Java Encoding[file.encoding]: " + System.getProperty("file.encoding"));
        
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        
        if (args != null && args.size() > 0) {
        	for(int i = 0; i < args.size(); i++) {
        		log.info("Java Args[" + i + "]: " + args.get(i));
        	}
        }
        
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
		if (gcs != null && gcs.size() > 0) {
			for (int i = 0; i < gcs.size(); i++) {
				log.info("Java GC[" + i + "]: " + gcs.get(i).getName());
			}
		}
	}
	
	private static boolean read(String path) {

		boolean result = true;
		prop = new Properties();

		try {
			prop.load(new FileInputStream(path + "/conf/server.properties"));

			String httpHostProp = prop.getProperty("http.host");
			if (httpHostProp != null) {
				HTTP_HOST = httpHostProp;
			}

			String httpPortProp = prop.getProperty("http.port");
			if (httpPortProp != null) {
				HTTP_PORT = Integer.parseInt(httpPortProp);
			}
			
			String httpIOThreadsProp = prop.getProperty("http.io_threads");
			if (httpIOThreadsProp != null) {
				HTTP_IO_THREADS = Integer.parseInt(httpIOThreadsProp);
			}
			
			String httpWorkerThreadsProp = prop.getProperty("http.worker_threads");
			if (httpWorkerThreadsProp != null) {
				HTTP_WORKER_THREADS = Integer.parseInt(httpWorkerThreadsProp);
			}

			String httpSendBufferSizeProp = prop.getProperty("http.send_buffer_size");
			if (httpSendBufferSizeProp != null) {
				HTTP_SEND_BUFFER_SIZE = Integer.parseInt(httpSendBufferSizeProp);
			}

			String httpReceiveBufferSizeProp = prop.getProperty("http.receive_buffer_size");
			if (httpReceiveBufferSizeProp != null) {
				HTTP_RECEIVE_BUFFER_SIZE = Integer.parseInt(httpReceiveBufferSizeProp);
			}
			
			String httpIdleProp = prop.getProperty("http.idle");
			if (httpIdleProp != null) {
				HTTP_IDLE = Integer.parseInt(httpIdleProp);
			}
			
			String httpTcpNodelayProp = prop.getProperty("http.tcp_nodelay");
			if (httpTcpNodelayProp != null) {
				HTTP_TCP_NODELAY = Boolean.parseBoolean(httpTcpNodelayProp);
			}
			
			String httpMaxBacklogProp = prop.getProperty("http.max_backlog");
			if (httpMaxBacklogProp != null) {
				HTTP_MAX_BACKLOG = Integer.parseInt(httpMaxBacklogProp);
			}
			
			String hlsSegmentMaxProp = prop.getProperty("hls.segment_max");
			if (hlsSegmentMaxProp != null) {
				HLS_SEGMENT_MAX = Integer.parseInt(hlsSegmentMaxProp);
			}
			
			String HLSEncryptProp = prop.getProperty("hls.encrypt");
			if (HLSEncryptProp != null) {
				HLS_ENCRYPT = Boolean.parseBoolean(HLSEncryptProp);
			}
			
			String hlsSegmentTimeProp = prop.getProperty("hls.segment_time");
			if (hlsSegmentTimeProp != null) {
				HLS_SEGMENT_TIME = Integer.parseInt(hlsSegmentTimeProp);
			}

			String rtmpHostProp = prop.getProperty("rtmp.host");
			if (rtmpHostProp != null) {
				RTMP_HOST = rtmpHostProp;
			}

			String rtmpPortProp = prop.getProperty("rtmp.port");
			if (rtmpPortProp != null) {
				RTMP_PORT = Integer.parseInt(rtmpPortProp);
			}

			String rtmpIOThreadsProp = prop.getProperty("rtmp.io_threads");
			if (rtmpIOThreadsProp != null) {
				RTMP_IO_THREADS = Integer.parseInt(rtmpIOThreadsProp);
			}
			
			String rtmpWorkerThreadsProp = prop.getProperty("rtmp.worker_threads");
			if (rtmpWorkerThreadsProp != null) {
				RTMP_WORKER_THREADS = Integer.parseInt(rtmpWorkerThreadsProp);
			}

			String rtmpSendBufferSizeProp = prop.getProperty("rtmp.send_buffer_size");
			if (rtmpSendBufferSizeProp != null) {
				RTMP_SEND_BUFFER_SIZE = Integer.parseInt(rtmpSendBufferSizeProp);
			}

			String rtmpReceiveBufferSizeProp = prop.getProperty("rtmp.receive_buffer_size");
			if (rtmpReceiveBufferSizeProp != null) {
				RTMP_RECEIVE_BUFFER_SIZE = Integer.parseInt(rtmpReceiveBufferSizeProp);
			}

			String rtmpPingIntervalProp = prop.getProperty("rtmp.ping_interval");
			if (rtmpPingIntervalProp != null) {
				RTMP_PING_INTERVAL = Integer.parseInt(rtmpPingIntervalProp);
			}

			String rtmpMaxInactivityProp = prop.getProperty("rtmp.max_inactivity");
			if (rtmpMaxInactivityProp != null) {
				RTMP_MAX_INACTIVITY = Integer.parseInt(rtmpMaxInactivityProp);
			}
			
			String rtmpMaxHandshakeTimeoutProp = prop.getProperty("rtmp.max_handshake_timeout");
			if (rtmpMaxInactivityProp != null) {
				RTMP_MAX_HANDSHAKE_TIMEOUT = Integer.parseInt(rtmpMaxHandshakeTimeoutProp);
			}
			

			String rtmpTcpNodelayProp = prop.getProperty("rtmp.tcp_nodelay");
			if (rtmpTcpNodelayProp != null) {
				RTMP_TCP_NODELAY = Boolean.parseBoolean(rtmpTcpNodelayProp);
			}
			
			String rtmpMaxBacklogProp = prop.getProperty("rtmp.max_backlog");
			if (rtmpMaxBacklogProp != null) {
				RTMP_MAX_BACKLOG = Integer.parseInt(rtmpMaxBacklogProp);
			}
						
			String rtmpDefaultServerBandwidthProp = prop.getProperty("rtmp.default_server_bandwidth");
			if (rtmpDefaultServerBandwidthProp != null) {
				RTMP_DEFAULT_SERVER_BANDWIDTH = Integer.parseInt(rtmpDefaultServerBandwidthProp);
			}
			
			String rtmpDefaultClientBandwidthProp = prop.getProperty("rtmp.default_client_bandwidth");
			if (rtmpDefaultClientBandwidthProp != null) {
				RTMP_DEFAULT_CLIENT_BANDWIDTH = Integer.parseInt(rtmpDefaultClientBandwidthProp);
			}
			
			String rtmpClientBandwidthLimitTypeProp = prop.getProperty("rtmp.client_bandwidth_limit_type");
			if (rtmpClientBandwidthLimitTypeProp != null) {
				RTMP_CLIENT_BANDWIDTH_LIMIT_TYPE = Integer.parseInt(rtmpClientBandwidthLimitTypeProp);
			}	
			
			String rtmpBandwidthDetectionProp = prop.getProperty("rtmp.bandwidth_detection");
			if (rtmpBandwidthDetectionProp != null) {
				RTMP_BANDWIDTH_DETECTION = Boolean.parseBoolean(rtmpBandwidthDetectionProp);
			}
			
			String rtspHostProp = prop.getProperty("rtsp.host");
			if (rtspHostProp != null) {
				RTSP_HOST = rtspHostProp;
			}

			String rtspPortProp = prop.getProperty("rtsp.port");
			if (rtspPortProp != null) {
				RTSP_PORT = Integer.parseInt(rtspPortProp);
			}

			String rtspIOThreadsProp = prop.getProperty("rtsp.io_threads");
			if (rtspIOThreadsProp != null) {
				RTSP_IO_THREADS = Integer.parseInt(rtspIOThreadsProp);
			}
			
			String rtspWorkerThreadsProp = prop.getProperty("rtsp.worker_threads");
			if (rtspWorkerThreadsProp != null) {
				RTSP_WORKER_THREADS = Integer.parseInt(rtspWorkerThreadsProp);
			}

			String rtspSendBufferSizeProp = prop.getProperty("rtsp.send_buffer_size");
			if (rtspSendBufferSizeProp != null) {
				RTSP_SEND_BUFFER_SIZE = Integer.parseInt(rtspSendBufferSizeProp);
			}

			String rtspReceiveBufferSizeProp = prop.getProperty("rtsp.receive_buffer_size");
			if (rtspReceiveBufferSizeProp != null) {
				RTSP_RECEIVE_BUFFER_SIZE = Integer.parseInt(rtspReceiveBufferSizeProp);
			}
			
			String rtspTcpNodelayProp = prop.getProperty("rtsp.tcp_nodelay");
			if (rtspTcpNodelayProp != null) {
				RTSP_TCP_NODELAY = Boolean.parseBoolean(rtspTcpNodelayProp);
			}
			
			String rtspMaxBacklogProp = prop.getProperty("rtsp.max_backlog");
			if (rtspMaxBacklogProp != null) {
				RTSP_MAX_BACKLOG = Integer.parseInt(rtspMaxBacklogProp);
			}
			
			String jmxHostProp = prop.getProperty("jmx.rmi.host");
			if (jmxHostProp != null) {
				JMX_RMI_HOST = jmxHostProp;
			}

			String jmxPortProp = prop.getProperty("jmx.rmi.port.registry");
			if (jmxPortProp != null) {
				JMX_RMI_PORT_REGISTRY = Integer.parseInt(jmxPortProp);
			}
			
			String jmxPortRemoteObjectsProp = prop.getProperty("jmx.rmi.port.remoteobjects");
			if (jmxPortRemoteObjectsProp != null) {
				JMX_RMI_PORT_REMOTEOBJECTS = jmxPortRemoteObjectsProp;
			}
			
			String jmxRmiEnableProp = prop.getProperty("jmx.rmi.enable");
			if (jmxRmiEnableProp != null) {
				JMX_RMI_ENABLE = Boolean.parseBoolean(jmxRmiEnableProp);
			}
			
			String notifySystimerTickProp = prop.getProperty("notify.systimer.tick");
			if (notifySystimerTickProp != null) {
				NOTIFY_SYSTIMER_TICK = Long.parseLong(notifySystimerTickProp);
			}
			
			String filecacheMaxsizeProp = prop.getProperty("filecache_maxsize");
			if (filecacheMaxsizeProp != null) {
				FILECACHE_MAXSIZE = Integer.parseInt(filecacheMaxsizeProp);
			}
			
			String filecachePurgeProp = prop.getProperty("filecache_purge");
			if (filecachePurgeProp != null) {
				FILECACHE_PURGE = Integer.parseInt(filecachePurgeProp);
			}
			
			String cacheIntervalProp = prop.getProperty("cache_interval");
			if (cacheIntervalProp != null) {
				CACHE_INTERVAL = Integer.parseInt(cacheIntervalProp);
			}
			
			String udpPortStartProp = prop.getProperty("udp.port_start");
			if(udpPortStartProp != null) {
				UDP_PORT_START = Integer.parseInt(udpPortStartProp);
			}
			
			String multicastExecutorThreadsProp = prop.getProperty("multicast.executor_threads");
			if(multicastExecutorThreadsProp != null) {
				MULTICAST_EXECUTOR_THREADS = Integer.parseInt(multicastExecutorThreadsProp);
			}
			
			String unicastExecutorThreadsProp = prop.getProperty("unicast.executor_threads");
			if(unicastExecutorThreadsProp != null) {
				UNICAST_EXECUTOR_THREADS = Integer.parseInt(unicastExecutorThreadsProp);
			}
			
			String mgrUrlProp = prop.getProperty("plugins.mgr_url");
			if (mgrUrlProp != null) {
				MGR_CONN_URL = mgrUrlProp;
			}
			
			String mgrSharedirProp = prop.getProperty("plugins.mgr_sharedir");
			if (mgrSharedirProp != null) {
				MGR_SHARE_DIR = mgrSharedirProp;
			}
			
			String mgrHaspVendorCodeProp = prop.getProperty("plugins.mgr.hasp_vendorCode");
			if (mgrHaspVendorCodeProp != null) {
				MGR_HASP_VENDORCODE = mgrHaspVendorCodeProp;
			}
			
			String mgrHaspFeatureIdProp = prop.getProperty("plugins.mgr.hasp_featureId");
			if (mgrHaspFeatureIdProp != null) {
				MGR_HASP_FEATUREID = Long.parseLong(mgrHaspFeatureIdProp);
			}
		} catch (FileNotFoundException e) {
			result = false;
		} catch (IOException e) {
			result = false;
		}
		return result;
	}
	
	private static boolean readApp(String path) {
	
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document dom = null;
		String webappPath = path+"/webapps";
		List<String> webApps = FileUtil.getDirectoryList(webappPath);
		ScopeContextBean scopeCtxBean = null;
		try {
			db = dbf.newDocumentBuilder();
			for(String app : webApps) {
				
				dom = db.parse(String.format("%s/%s/WEB-INF/Application.xml", webappPath, app));
				scopeCtxBean = new ScopeContextBean();
				readParseAppDom(dom, scopeCtxBean);
				appConfigMap.put(app, scopeCtxBean);
			}
		}catch(Exception e) {
			return false;
		}
		return true;
	}
	
	private static void readParseAppDom(Document dom, ScopeContextBean ctxBean) {
		
		Element rootEl = dom.getDocumentElement();
		
		ctxBean.addClazz(ScopeContextBean.RTMPAPPLICATIONADAPTER_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.RTMPAPPLICATIONADAPTER_BEAN));
		ctxBean.addClazz(ScopeContextBean.HTTPAPPLICATIONADAPTER_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.HTTPAPPLICATIONADAPTER_BEAN));
		ctxBean.addClazz(ScopeContextBean.BROADCASTSTREAM_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.BROADCASTSTREAM_BEAN));
		ctxBean.addClazz(ScopeContextBean.SUBSCRIBERSTREAM_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.SUBSCRIBERSTREAM_BEAN));
		ctxBean.addClazz(ScopeContextBean.SINGLESTREAM_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.SINGLESTREAM_BEAN));
		ctxBean.addClazz(ScopeContextBean.PROVIDERSERVICE_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.PROVIDERSERVICE_BEAN));
		ctxBean.addClazz(ScopeContextBean.CONSUMERSERVICE_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.CONSUMERSERVICE_BEAN));
		ctxBean.addClazz(ScopeContextBean.STREAMSERVICE_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.STREAMSERVICE_BEAN));
		ctxBean.addClazz(ScopeContextBean.FILECONSUMER_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.FILECONSUMER_BEAN));
		ctxBean.addClazz(ScopeContextBean.RTMPSAMPLEACCESS_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.RTMPSAMPLEACCESS_BEAN));
		ctxBean.addClazz(ScopeContextBean.SECURITYFORBIDDEN_BEAN, Configuration.getTextValue(rootEl, ScopeContextBean.SECURITYFORBIDDEN_BEAN));
	}
	
	public static ContextBean getTextValue(Element ele, String tagName) {
		String textVal = null;
		ContextBean ctxBean = new ContextBean();
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
			Node proNode = null;
			for(int i = 0; i < el.getAttributes().getLength(); i++) {
				
				proNode = el.getAttributes().item(i);
				ctxBean.setProperty(proNode.getNodeName(), proNode.getNodeValue());
			}
		}
		ctxBean.setClassName(StringUtils.trimToNull(textVal));
		return ctxBean;
	}
	
	public static String get(String key){
	
		return prop.getProperty(key);
	}
	
	public static String get(String key,String defaultValue){
		 
		return prop.getProperty(key, defaultValue);
	}
}
