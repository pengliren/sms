package com.sms.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置验证器
 * 
 * 在服务器启动前验证配置的正确性，提前发现问题
 * 
 * @author SMS Team
 */
public class ConfigValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigValidator.class);
    
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    
    /**
     * 验证服务器配置
     */
    public ValidationResult validateConfiguration(String configRoot) {
        logger.info("开始验证服务器配置...");
        
        errors.clear();
        warnings.clear();
        
        // 验证配置文件存在性
        validateConfigFiles(configRoot);
        
        // 验证服务器配置
        validateServerProperties(configRoot);
        
        // 验证日志配置
        validateLogbackConfig(configRoot);
        
        // 验证端口可用性
        validatePortAvailability(configRoot);
        
        // 验证目录权限
        validateDirectoryPermissions(configRoot);
        
        ValidationResult result = new ValidationResult(
            new ArrayList<>(errors), 
            new ArrayList<>(warnings)
        );
        
        if (errors.isEmpty()) {
            logger.info("配置验证通过");
        } else {
            logger.error("配置验证失败，发现 {} 个错误", errors.size());
        }
        
        if (!warnings.isEmpty()) {
            logger.warn("配置验证发现 {} 个警告", warnings.size());
        }
        
        return result;
    }
    
    /**
     * 验证配置文件存在性
     */
    private void validateConfigFiles(String configRoot) {
        String[] requiredFiles = {
            "server.properties",
            "logback.xml"
        };
        
        for (String fileName : requiredFiles) {
            File configFile = new File(configRoot, fileName);
            if (!configFile.exists()) {
                errors.add("必需的配置文件不存在: " + configFile.getAbsolutePath());
            } else if (!configFile.canRead()) {
                errors.add("配置文件不可读: " + configFile.getAbsolutePath());
            }
        }
    }
    
    /**
     * 验证服务器属性配置
     */
    private void validateServerProperties(String configRoot) {
        File propertiesFile = new File(configRoot, "server.properties");
        if (!propertiesFile.exists()) {
            return; // 已在上面报告过错误
        }
        
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            props.load(fis);
            
            // 验证HTTP配置
            validateHttpConfig(props);
            
            // 验证RTMP配置
            validateRtmpConfig(props);
            
            // 验证RTSP配置
            validateRtspConfig(props);
            
            // 验证JMX配置
            validateJmxConfig(props);
            
            // 验证缓存配置
            validateCacheConfig(props);
            
        } catch (IOException e) {
            errors.add("无法读取配置文件 server.properties: " + e.getMessage());
        }
    }
    
    /**
     * 验证HTTP配置
     */
    private void validateHttpConfig(Properties props) {
        // 验证HTTP端口
        String httpPort = props.getProperty("http.port", "5080");
        if (!isValidPort(httpPort)) {
            errors.add("HTTP端口配置无效: " + httpPort);
        }
        
        // 验证HTTP主机
        String httpHost = props.getProperty("http.host", "0.0.0.0");
        if (!isValidHost(httpHost)) {
            errors.add("HTTP主机配置无效: " + httpHost);
        }
        
        // 验证线程配置
        String ioThreads = props.getProperty("http.io_threads", "2");
        if (!isValidThreadCount(ioThreads)) {
            warnings.add("HTTP IO线程数配置可能不合适: " + ioThreads);
        }
        
        String workerThreads = props.getProperty("http.worker_threads", "32");
        if (!isValidThreadCount(workerThreads)) {
            warnings.add("HTTP工作线程数配置可能不合适: " + workerThreads);
        }
        
        // 验证缓冲区大小
        String sendBuffer = props.getProperty("http.send_buffer_size", "16000");
        if (!isValidBufferSize(sendBuffer)) {
            warnings.add("HTTP发送缓冲区大小配置可能不合适: " + sendBuffer);
        }
        
        String receiveBuffer = props.getProperty("http.receive_buffer_size", "16000");
        if (!isValidBufferSize(receiveBuffer)) {
            warnings.add("HTTP接收缓冲区大小配置可能不合适: " + receiveBuffer);
        }
    }
    
    /**
     * 验证RTMP配置
     */
    private void validateRtmpConfig(Properties props) {
        // 验证RTMP端口
        String rtmpPort = props.getProperty("rtmp.port", "1935");
        if (!isValidPort(rtmpPort)) {
            errors.add("RTMP端口配置无效: " + rtmpPort);
        }
        
        // 验证RTMP主机
        String rtmpHost = props.getProperty("rtmp.host", "0.0.0.0");
        if (!isValidHost(rtmpHost)) {
            errors.add("RTMP主机配置无效: " + rtmpHost);
        }
        
        // 验证带宽配置
        String serverBandwidth = props.getProperty("rtmp.default_server_bandwidth", "2500000");
        if (!isValidBandwidth(serverBandwidth)) {
            warnings.add("RTMP服务器带宽配置可能不合适: " + serverBandwidth);
        }
        
        String clientBandwidth = props.getProperty("rtmp.default_client_bandwidth", "2500000");
        if (!isValidBandwidth(clientBandwidth)) {
            warnings.add("RTMP客户端带宽配置可能不合适: " + clientBandwidth);
        }
        
        // 验证超时配置
        String pingInterval = props.getProperty("rtmp.ping_interval", "5000");
        if (!isValidTimeout(pingInterval)) {
            warnings.add("RTMP ping间隔配置可能不合适: " + pingInterval);
        }
        
        String maxInactivity = props.getProperty("rtmp.max_inactivity", "60000");
        if (!isValidTimeout(maxInactivity)) {
            warnings.add("RTMP最大非活跃时间配置可能不合适: " + maxInactivity);
        }
    }
    
    /**
     * 验证RTSP配置
     */
    private void validateRtspConfig(Properties props) {
        // 验证RTSP端口
        String rtspPort = props.getProperty("rtsp.port", "554");
        if (!isValidPort(rtspPort)) {
            errors.add("RTSP端口配置无效: " + rtspPort);
        }
        
        // 验证UDP端口范围
        String udpPortStart = props.getProperty("udp.port_start", "6970");
        if (!isValidPort(udpPortStart)) {
            errors.add("UDP起始端口配置无效: " + udpPortStart);
        } else {
            int startPort = Integer.parseInt(udpPortStart);
            if (startPort < 1024) {
                warnings.add("UDP起始端口小于1024，可能需要管理员权限: " + startPort);
            }
        }
    }
    
    /**
     * 验证JMX配置
     */
    private void validateJmxConfig(Properties props) {
        String jmxEnable = props.getProperty("jmx.rmi.enable", "false");
        if ("true".equalsIgnoreCase(jmxEnable)) {
            String jmxPort = props.getProperty("jmx.rmi.port.registry", "9999");
            if (!isValidPort(jmxPort)) {
                errors.add("JMX端口配置无效: " + jmxPort);
            }
            
            String jmxHost = props.getProperty("jmx.rmi.host", "0.0.0.0");
            if (!isValidHost(jmxHost)) {
                errors.add("JMX主机配置无效: " + jmxHost);
            }
        }
    }
    
    /**
     * 验证缓存配置
     */
    private void validateCacheConfig(Properties props) {
        String cacheMaxSize = props.getProperty("filecache_maxsize", "500");
        try {
            int maxSize = Integer.parseInt(cacheMaxSize);
            if (maxSize <= 0) {
                warnings.add("文件缓存最大大小配置无效: " + cacheMaxSize);
            } else if (maxSize > 2048) {
                warnings.add("文件缓存最大大小过大，可能消耗过多内存: " + cacheMaxSize + "MB");
            }
        } catch (NumberFormatException e) {
            warnings.add("文件缓存最大大小配置格式错误: " + cacheMaxSize);
        }
        
        String cachePurge = props.getProperty("filecache_purge", "10");
        try {
            int purgeInterval = Integer.parseInt(cachePurge);
            if (purgeInterval <= 0) {
                warnings.add("文件缓存清理间隔配置无效: " + cachePurge);
            }
        } catch (NumberFormatException e) {
            warnings.add("文件缓存清理间隔配置格式错误: " + cachePurge);
        }
    }
    
    /**
     * 验证日志配置
     */
    private void validateLogbackConfig(String configRoot) {
        File logbackFile = new File(configRoot, "logback.xml");
        if (!logbackFile.exists()) {
            return; // 已在上面报告过错误
        }
        
        // 检查日志文件大小（简单检查）
        if (logbackFile.length() == 0) {
            warnings.add("logback.xml文件为空");
        }
        
        // 可以添加更详细的XML解析验证
    }
    
    /**
     * 验证端口可用性
     */
    private void validatePortAvailability(String configRoot) {
        File propertiesFile = new File(configRoot, "server.properties");
        if (!propertiesFile.exists()) {
            return;
        }
        
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            props.load(fis);
            
            // 检查主要端口
            checkPortAvailability("HTTP", props.getProperty("http.port", "5080"));
            checkPortAvailability("RTMP", props.getProperty("rtmp.port", "1935"));
            checkPortAvailability("RTSP", props.getProperty("rtsp.port", "554"));
            
            String jmxEnable = props.getProperty("jmx.rmi.enable", "false");
            if ("true".equalsIgnoreCase(jmxEnable)) {
                checkPortAvailability("JMX", props.getProperty("jmx.rmi.port.registry", "9999"));
            }
            
        } catch (IOException e) {
            warnings.add("无法验证端口可用性: " + e.getMessage());
        }
    }
    
    /**
     * 检查单个端口可用性
     */
    private void checkPortAvailability(String serviceName, String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            try (ServerSocket socket = new ServerSocket(port)) {
                // 端口可用
            } catch (IOException e) {
                warnings.add(serviceName + "端口 " + port + " 不可用: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            // 端口格式错误，已在其他地方报告
        }
    }
    
    /**
     * 验证目录权限
     */
    private void validateDirectoryPermissions(String configRoot) {
        File configDir = new File(configRoot);
        File rootDir = configDir.getParentFile();
        
        // 检查配置目录权限
        if (!configDir.canRead()) {
            errors.add("配置目录不可读: " + configDir.getAbsolutePath());
        }
        
        // 检查根目录权限
        if (rootDir != null) {
            if (!rootDir.canRead()) {
                errors.add("根目录不可读: " + rootDir.getAbsolutePath());
            }
            if (!rootDir.canWrite()) {
                warnings.add("根目录不可写，可能影响日志和临时文件创建: " + rootDir.getAbsolutePath());
            }
        }
        
        // 检查webapps目录
        File webappsDir = new File(rootDir, "webapps");
        if (webappsDir.exists() && !webappsDir.canRead()) {
            warnings.add("webapps目录不可读: " + webappsDir.getAbsolutePath());
        }
        
        // 检查plugins目录
        File pluginsDir = new File(rootDir, "plugins");
        if (pluginsDir.exists() && !pluginsDir.canRead()) {
            warnings.add("plugins目录不可读: " + pluginsDir.getAbsolutePath());
        }
    }
    
    /**
     * 验证端口号是否有效
     */
    private boolean isValidPort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            return port > 0 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 验证主机地址是否有效
     */
    private boolean isValidHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            return false;
        }
        
        if ("0.0.0.0".equals(host) || "localhost".equals(host)) {
            return true;
        }
        
        try {
            InetAddress.getByName(host);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证线程数是否合理
     */
    private boolean isValidThreadCount(String threadStr) {
        try {
            int threads = Integer.parseInt(threadStr);
            return threads > 0 && threads <= 1000; // 合理范围
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 验证缓冲区大小是否合理
     */
    private boolean isValidBufferSize(String bufferStr) {
        try {
            int buffer = Integer.parseInt(bufferStr);
            return buffer >= 1024 && buffer <= 1024 * 1024; // 1KB到1MB
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 验证带宽配置是否合理
     */
    private boolean isValidBandwidth(String bandwidthStr) {
        try {
            long bandwidth = Long.parseLong(bandwidthStr);
            return bandwidth > 0 && bandwidth <= 1000000000L; // 最大1Gbps
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 验证超时配置是否合理
     */
    private boolean isValidTimeout(String timeoutStr) {
        try {
            int timeout = Integer.parseInt(timeoutStr);
            return timeout >= 1000 && timeout <= 300000; // 1秒到5分钟
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(List<String> errors, List<String> warnings) {
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public void printReport() {
            if (!errors.isEmpty()) {
                System.err.println("配置错误:");
                for (String error : errors) {
                    System.err.println("  [错误] " + error);
                }
            }
            
            if (!warnings.isEmpty()) {
                System.out.println("配置警告:");
                for (String warning : warnings) {
                    System.out.println("  [警告] " + warning);
                }
            }
            
            if (errors.isEmpty() && warnings.isEmpty()) {
                System.out.println("配置验证通过，未发现问题。");
            }
        }
    }
}