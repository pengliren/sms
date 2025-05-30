package com.sms.server;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.classloading.ClassLoaderBuilder;

/**
 * 优化的服务器启动器
 * 
 * 主要优化点：
 * 1. 异步并行启动各个组件
 * 2. 更好的异常处理和错误恢复
 * 3. 启动进度监控
 * 4. 健康检查机制
 * 5. 优雅关闭
 * 
 * @author SMS Team
 */
public class OptimizedBootstrap {
    
    private static final Logger logger = LoggerFactory.getLogger(OptimizedBootstrap.class);
    
    // 启动阶段枚举
    private enum StartupPhase {
        INIT("初始化", 1),
        CLASSLOADER("类加载器", 2),
        LOGGING("日志系统", 3),
        CONFIG("配置系统", 4),
        JMX("JMX代理", 5),
        APP("应用加载", 6),
        NETWORK("网络服务", 7),
        PLUGINS("插件系统", 8),
        COMPLETE("启动完成", 9);
        
        private final String description;
        private final int order;
        
        StartupPhase(String description, int order) {
            this.description = description;
            this.order = order;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getOrder() {
            return order;
        }
    }
    
    // 启动状态跟踪
    private static final AtomicInteger currentPhase = new AtomicInteger(0);
    private static final AtomicLong startTime = new AtomicLong();
    private static volatile boolean shutdownRequested = false;
    
    // 组件引用，用于优雅关闭
    private static volatile Object jmxAgent;
    private static volatile Object appLoader;
    private static volatile Object rtmpTransport;
    private static volatile Object httpTransport;
    private static volatile Object rtspTransport;
    private static volatile Object pluginLauncher;
    
    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "start":
                    startServerOptimized();
                    break;
                case "stop":
                    stopServer();
                    break;
                case "status":
                    showStatus();
                    break;
                default:
                    System.out.println("Usage: java OptimizedBootstrap [start|stop|status]");
                    System.exit(1);
            }
        } else {
            System.out.println("Usage: java OptimizedBootstrap [start|stop|status]");
            System.exit(1);
        }
    }
    
    /**
     * 优化的服务器启动方法
     */
    private static void startServerOptimized() {
        startTime.set(System.currentTimeMillis());
        StartupProfiler profiler = StartupProfiler.getInstance();
        profiler.startGlobalProfiling();
        
        try {
            logger.info("=== SMS 流媒体服务器启动开始 ===");
            
            // 阶段1: 初始化
            updatePhase(StartupPhase.INIT);
            profiler.startPhase("初始化");
            String root = getSrvRoot();
            String conf = getConfigurationRoot(root);
            validateEnvironment(root, conf);
            
            // 配置验证
            ConfigValidator validator = new ConfigValidator();
            ConfigValidator.ValidationResult validationResult = validator.validateConfiguration(conf);
            if (!validationResult.isValid()) {
                logger.error("配置验证失败:");
                for (String error : validationResult.getErrors()) {
                    logger.error("  {}", error);
                }
                throw new RuntimeException("配置验证失败");
            }
            
            if (!validationResult.getWarnings().isEmpty()) {
                logger.warn("配置验证发现警告:");
                for (String warning : validationResult.getWarnings()) {
                    logger.warn("  {}", warning);
                }
            }
            profiler.endPhase("初始化");
            
            // 阶段2: 类加载器
            updatePhase(StartupPhase.CLASSLOADER);
            profiler.startPhase("类加载器");
            ClassLoader baseLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader loader = ClassLoaderBuilder.build();
            Thread.currentThread().setContextClassLoader(loader);
            profiler.endPhase("类加载器");
            
            // 阶段3: 日志系统
            updatePhase(StartupPhase.LOGGING);
            profiler.startPhase("日志系统");
            initializeLogging(loader, conf);
            profiler.endPhase("日志系统");
            
            // 阶段4: 配置系统
            updatePhase(StartupPhase.CONFIG);
            profiler.startPhase("配置系统");
            boolean configOk = initializeConfiguration(loader, root);
            if (!configOk) {
                throw new RuntimeException("配置初始化失败");
            }
            profiler.endPhase("配置系统");
            
            // 阶段5: JMX代理
            updatePhase(StartupPhase.JMX);
            profiler.startPhase("JMX代理");
            jmxAgent = initializeJMX(loader);
            profiler.endPhase("JMX代理");
            
            // 阶段6: 应用加载
            updatePhase(StartupPhase.APP);
            profiler.startPhase("应用加载");
            appLoader = initializeApplication(loader);
            profiler.endPhase("应用加载");
            
            // 阶段7: 网络服务（并行启动）
            updatePhase(StartupPhase.NETWORK);
            profiler.startPhase("网络服务");
            initializeNetworkServices(loader);
            profiler.endPhase("网络服务");
            
            // 阶段8: 插件系统
            updatePhase(StartupPhase.PLUGINS);
            profiler.startPhase("插件系统");
            pluginLauncher = initializePlugins(loader);
            profiler.endPhase("插件系统");
            
            // 恢复原始类加载器
            Thread.currentThread().setContextClassLoader(baseLoader);
            
            // 注册关闭钩子
            registerShutdownHook();
            
            // 阶段9: 启动完成
            updatePhase(StartupPhase.COMPLETE);
            long totalTime = System.currentTimeMillis() - startTime.get();
            logger.info("=== SMS 流媒体服务器启动完成，耗时: {}ms ===", totalTime);
            
            // 生成性能报告
            profiler.generateReport();
            profiler.generateOptimizationSuggestions();
            
            // 启动健康检查
            startHealthCheck();
            
        } catch (Exception e) {
            logger.error("服务器启动失败", e);
            performGracefulShutdown();
            System.exit(1);
        }
    }
    
    /**
     * 验证运行环境
     */
    private static void validateEnvironment(String root, String conf) {
        logger.info("验证运行环境...");
        
        // 检查根目录
        File rootDir = new File(root);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new RuntimeException("根目录不存在或不是目录: " + root);
        }
        
        // 检查配置目录
        File confDir = new File(conf);
        if (!confDir.exists() || !confDir.isDirectory()) {
            throw new RuntimeException("配置目录不存在或不是目录: " + conf);
        }
        
        // 检查必要的配置文件
        String[] requiredFiles = {"logback.xml", "server.properties"};
        for (String file : requiredFiles) {
            File configFile = new File(confDir, file);
            if (!configFile.exists()) {
                throw new RuntimeException("必要的配置文件不存在: " + configFile.getAbsolutePath());
            }
        }
        
        logger.info("环境验证通过");
    }
    
    /**
     * 初始化日志系统
     */
    private static void initializeLogging(ClassLoader loader, String conf) throws Exception {
        logger.info("初始化日志系统...");
        
        Class<?> joranConfigurator = Class.forName("ch.qos.logback.classic.joran.JoranConfigurator", true, loader);
        Class<?> loggerFactory = Class.forName("org.slf4j.LoggerFactory", false, loader);
        
        Object configurator = joranConfigurator.newInstance();
        Object loggerContext = loggerFactory.getMethod("getILoggerFactory").invoke(null);
        
        Class.forName("ch.qos.logback.classic.LoggerContext", false, loader)
            .getMethod("reset").invoke(loggerContext);
        
        joranConfigurator.getMethod("setContext", Class.forName("ch.qos.logback.core.Context", false, loader))
            .invoke(configurator, loggerContext);
        
        joranConfigurator.getMethod("doConfigure", File.class)
            .invoke(configurator, new File(conf, "logback.xml"));
        
        logger.info("日志系统初始化完成");
    }
    
    /**
     * 初始化配置系统
     */
    private static boolean initializeConfiguration(ClassLoader loader, String root) throws Exception {
        logger.info("初始化配置系统...");
        
        Class<?> configClass = Class.forName("com.sms.server.Configuration", true, loader);
        Method configMethod = configClass.getMethod("initSystemConfig", String.class);
        boolean result = (Boolean) configMethod.invoke(null, root);
        
        if (result) {
            logger.info("配置系统初始化完成");
        } else {
            logger.error("配置系统初始化失败");
        }
        
        return result;
    }
    
    /**
     * 初始化JMX代理
     */
    private static Object initializeJMX(ClassLoader loader) throws Exception {
        logger.info("初始化JMX代理...");
        
        Object jmxAgent = Class.forName("com.sms.jmx.JMXAgent", true, loader).newInstance();
        Method initMethod = jmxAgent.getClass().getMethod("init");
        initMethod.invoke(jmxAgent);
        
        logger.info("JMX代理初始化完成");
        return jmxAgent;
    }
    
    /**
     * 初始化应用加载器
     */
    private static Object initializeApplication(ClassLoader loader) throws Exception {
        logger.info("初始化应用加载器...");
        
        Object appLoader = Class.forName("com.sms.server.ServerAppLoader", true, loader).newInstance();
        Method startMethod = appLoader.getClass().getMethod("start");
        startMethod.invoke(appLoader);
        
        logger.info("应用加载器初始化完成");
        return appLoader;
    }
    
    /**
     * 并行初始化网络服务
     */
    private static void initializeNetworkServices(ClassLoader loader) throws Exception {
        logger.info("初始化网络服务...");
        
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        try {
            // 并行启动三个网络服务
            CompletableFuture<Object> rtmpFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    logger.info("启动RTMP服务...");
                    Object transport = Class.forName("com.sms.server.net.rtmp.RTMPMinaTransport", true, loader).newInstance();
                    Method startMethod = transport.getClass().getMethod("start");
                    startMethod.invoke(transport);
                    logger.info("RTMP服务启动完成");
                    return transport;
                } catch (Exception e) {
                    logger.error("RTMP服务启动失败", e);
                    throw new RuntimeException("RTMP服务启动失败", e);
                }
            }, executor);
            
            CompletableFuture<Object> httpFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    logger.info("启动HTTP服务...");
                    Object transport = Class.forName("com.sms.server.net.http.HTTPMinaTransport", true, loader).newInstance();
                    Method startMethod = transport.getClass().getMethod("start");
                    startMethod.invoke(transport);
                    logger.info("HTTP服务启动完成");
                    return transport;
                } catch (Exception e) {
                    logger.error("HTTP服务启动失败", e);
                    throw new RuntimeException("HTTP服务启动失败", e);
                }
            }, executor);
            
            CompletableFuture<Object> rtspFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    logger.info("启动RTSP服务...");
                    Object transport = Class.forName("com.sms.server.net.rtsp.RTSPMinaTransport", true, loader).newInstance();
                    Method startMethod = transport.getClass().getMethod("start");
                    startMethod.invoke(transport);
                    logger.info("RTSP服务启动完成");
                    return transport;
                } catch (Exception e) {
                    logger.error("RTSP服务启动失败", e);
                    throw new RuntimeException("RTSP服务启动失败", e);
                }
            }, executor);
            
            // 等待所有服务启动完成
            CompletableFuture.allOf(rtmpFuture, httpFuture, rtspFuture)
                .get(30, TimeUnit.SECONDS); // 30秒超时
            
            rtmpTransport = rtmpFuture.get();
            httpTransport = httpFuture.get();
            rtspTransport = rtspFuture.get();
            
            logger.info("所有网络服务启动完成");
            
        } finally {
            executor.shutdown();
        }
    }
    
    /**
     * 初始化插件系统
     */
    private static Object initializePlugins(ClassLoader loader) throws Exception {
        logger.info("初始化插件系统...");
        
        Object pluginLauncher = Class.forName("com.sms.server.plugin.PluginLauncher", true, loader).newInstance();
        Method startMethod = pluginLauncher.getClass().getMethod("start");
        startMethod.invoke(pluginLauncher);
        
        logger.info("插件系统初始化完成");
        return pluginLauncher;
    }
    
    /**
     * 注册关闭钩子
     */
    private static void registerShutdownHook() {
        logger.info("注册关闭钩子...");
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("收到关闭信号，开始优雅关闭...");
            shutdownRequested = true;
            performGracefulShutdown();
        }, "SMS-Shutdown-Hook"));
    }
    
    /**
     * 执行优雅关闭
     */
    private static void performGracefulShutdown() {
        try {
            logger.info("开始优雅关闭服务器...");
            
            // 按相反顺序关闭组件
            if (pluginLauncher != null) {
                logger.info("关闭插件系统...");
                Method destroyMethod = pluginLauncher.getClass().getMethod("destroy");
                destroyMethod.invoke(pluginLauncher);
            }
            
            // 关闭网络服务
            shutdownNetworkServices();
            
            logger.info("服务器已优雅关闭");
            
        } catch (Exception e) {
            logger.error("关闭过程中发生错误", e);
        }
    }
    
    /**
     * 关闭网络服务
     */
    private static void shutdownNetworkServices() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        try {
            CompletableFuture<Void> rtmpShutdown = CompletableFuture.runAsync(() -> {
                if (rtmpTransport != null) {
                    try {
                        logger.info("关闭RTMP服务...");
                        Method stopMethod = rtmpTransport.getClass().getMethod("stop");
                        stopMethod.invoke(rtmpTransport);
                        logger.info("RTMP服务已关闭");
                    } catch (Exception e) {
                        logger.error("关闭RTMP服务失败", e);
                    }
                }
            }, executor);
            
            CompletableFuture<Void> httpShutdown = CompletableFuture.runAsync(() -> {
                if (httpTransport != null) {
                    try {
                        logger.info("关闭HTTP服务...");
                        Method stopMethod = httpTransport.getClass().getMethod("stop");
                        stopMethod.invoke(httpTransport);
                        logger.info("HTTP服务已关闭");
                    } catch (Exception e) {
                        logger.error("关闭HTTP服务失败", e);
                    }
                }
            }, executor);
            
            CompletableFuture<Void> rtspShutdown = CompletableFuture.runAsync(() -> {
                if (rtspTransport != null) {
                    try {
                        logger.info("关闭RTSP服务...");
                        Method stopMethod = rtspTransport.getClass().getMethod("stop");
                        stopMethod.invoke(rtspTransport);
                        logger.info("RTSP服务已关闭");
                    } catch (Exception e) {
                        logger.error("关闭RTSP服务失败", e);
                    }
                }
            }, executor);
            
            CompletableFuture.allOf(rtmpShutdown, httpShutdown, rtspShutdown)
                .get(10, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            logger.error("关闭网络服务时发生错误", e);
        } finally {
            executor.shutdown();
        }
    }
    
    /**
     * 启动健康检查
     */
    private static void startHealthCheck() {
        ExecutorService healthCheckExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "SMS-HealthCheck");
            t.setDaemon(true);
            return t;
        });
        
        healthCheckExecutor.submit(() -> {
            while (!shutdownRequested) {
                try {
                    Thread.sleep(30000); // 30秒检查一次
                    
                    // 执行健康检查逻辑
                    performHealthCheck();
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.warn("健康检查过程中发生错误", e);
                }
            }
        });
    }
    
    /**
     * 执行健康检查
     */
    private static void performHealthCheck() {
        // 检查内存使用情况
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / totalMemory * 100;
        
        if (memoryUsage > 90) {
            logger.warn("内存使用率过高: {:.2f}%", memoryUsage);
        }
        
        logger.debug("健康检查完成 - 内存使用率: {:.2f}%", memoryUsage);
    }
    
    /**
     * 更新启动阶段
     */
    private static void updatePhase(StartupPhase phase) {
        currentPhase.set(phase.getOrder());
        long elapsed = System.currentTimeMillis() - startTime.get();
        logger.info("[" + phase.getOrder() + "/" + StartupPhase.values().length + "] " + 
            phase.getDescription() + " - 已耗时: " + elapsed + "ms");
    }
    
    /**
     * 显示服务器状态
     */
    private static void showStatus() {
        System.out.println("=== SMS 流媒体服务器状态 ===");
        
        if (currentPhase.get() == StartupPhase.COMPLETE.getOrder()) {
            long uptime = System.currentTimeMillis() - startTime.get();
            System.out.println("状态: 运行中");
            System.out.println("运行时间: " + formatDuration(uptime));
        } else if (currentPhase.get() > 0) {
            System.out.println("状态: 启动中");
            System.out.println("当前阶段: " + getCurrentPhaseDescription());
        } else {
            System.out.println("状态: 未启动");
        }
        
        // 显示内存信息
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        System.out.println("内存使用: " + formatBytes(usedMemory) + " / " + formatBytes(totalMemory));
        System.out.println("可用内存: " + formatBytes(freeMemory));
    }
    
    /**
     * 获取当前阶段描述
     */
    private static String getCurrentPhaseDescription() {
        int phase = currentPhase.get();
        for (StartupPhase sp : StartupPhase.values()) {
            if (sp.getOrder() == phase) {
                return sp.getDescription();
            }
        }
        return "未知阶段";
    }
    
    /**
     * 格式化持续时间
     */
    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d小时%d分钟%d秒", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }
    
    /**
     * 格式化字节数
     */
    private static String formatBytes(long bytes) {
        if (bytes >= 1024 * 1024 * 1024) {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        } else if (bytes >= 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else if (bytes >= 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return bytes + " B";
        }
    }
    
    /**
     * 停止服务器
     */
    private static void stopServer() {
        try {
            java.net.Socket socket = new java.net.Socket("127.0.0.1", 5080);
            java.io.OutputStream os = socket.getOutputStream();
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
            System.out.println("停止服务器失败: " + e.getMessage());
        }
    }
    
    private static String httpDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        return sdf.format(new java.util.Date());
    }
    
    /**
     * 获取服务器根目录
     */
    private static String getSrvRoot() {
        String root = System.getProperty("sms.root");
        if (root == null) {
            root = System.getenv("SMS_HOME");
        }
        if (root == null || ".".equals(root)) {
            root = System.getProperty("user.dir");
        }
        if (File.separatorChar != '/') {
            root = root.replaceAll("\\\\", "/");
        }
        if (root.charAt(root.length() - 1) == '/') {
            root = root.substring(0, root.length() - 1);
        }
        System.setProperty("sms.root", root);
        return root;
    }
    
    /**
     * 获取配置根目录
     */
    private static String getConfigurationRoot(String root) {
        String conf = System.getProperty("sms.config_root");
        if (root != null && conf == null) {
            conf = root + "/conf";
        }
        if (File.separatorChar != '/') {
            conf = conf.replaceAll("\\\\", "/");
        }
        System.setProperty("sms.config_root", conf);
        return conf;
    }
}