package com.sms.server;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动性能分析器
 * 
 * 用于监控和分析服务器启动过程中的性能指标
 * 
 * @author SMS Team
 */
public class StartupProfiler {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupProfiler.class);
    
    // 单例实例
    private static final StartupProfiler INSTANCE = new StartupProfiler();
    
    // 性能指标存储
    private final ConcurrentMap<String, Long> startTimes = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> endTimes = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> durations = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, MemorySnapshot> memorySnapshots = new ConcurrentHashMap<>();
    
    // 全局启动时间
    private final AtomicLong globalStartTime = new AtomicLong();
    
    // JMX Bean
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    private StartupProfiler() {
        // 私有构造函数
    }
    
    public static StartupProfiler getInstance() {
        return INSTANCE;
    }
    
    /**
     * 开始全局性能监控
     */
    public void startGlobalProfiling() {
        globalStartTime.set(System.currentTimeMillis());
        logger.info("启动性能监控开始");
    }
    
    /**
     * 开始监控指定阶段
     */
    public void startPhase(String phaseName) {
        long startTime = System.currentTimeMillis();
        startTimes.put(phaseName, startTime);
        
        // 记录内存快照
        MemorySnapshot snapshot = takeMemorySnapshot();
        memorySnapshots.put(phaseName + "_start", snapshot);
        
        logger.debug("开始监控阶段: {}", phaseName);
    }
    
    /**
     * 结束监控指定阶段
     */
    public void endPhase(String phaseName) {
        long endTime = System.currentTimeMillis();
        endTimes.put(phaseName, endTime);
        
        // 计算持续时间
        Long startTime = startTimes.get(phaseName);
        if (startTime != null) {
            long duration = endTime - startTime;
            durations.put(phaseName, duration);
            
            // 记录结束时的内存快照
            MemorySnapshot snapshot = takeMemorySnapshot();
            memorySnapshots.put(phaseName + "_end", snapshot);
            
            logger.info("阶段 {} 完成，耗时: {}ms", phaseName, duration);
        }
    }
    
    /**
     * 获取阶段持续时间
     */
    public long getPhaseDuration(String phaseName) {
        return durations.getOrDefault(phaseName, -1L);
    }
    
    /**
     * 获取总启动时间
     */
    public long getTotalStartupTime() {
        if (globalStartTime.get() == 0) {
            return -1;
        }
        return System.currentTimeMillis() - globalStartTime.get();
    }
    
    /**
     * 生成性能报告
     */
    public void generateReport() {
        logger.info("=== 启动性能报告 ===");
        
        long totalTime = getTotalStartupTime();
        if (totalTime > 0) {
            logger.info("总启动时间: {}ms", totalTime);
        }
        
        // 按阶段显示性能数据
        logger.info("各阶段性能数据:");
        durations.entrySet().stream()
            .sorted((e1, e2) -> {
                Long start1 = startTimes.get(e1.getKey());
                Long start2 = startTimes.get(e2.getKey());
                if (start1 == null || start2 == null) return 0;
                return start1.compareTo(start2);
            })
            .forEach(entry -> {
                String phase = entry.getKey();
                long duration = entry.getValue();
                double percentage = totalTime > 0 ? (duration * 100.0 / totalTime) : 0;
                
                logger.info("  " + phase + ": " + duration + "ms (" + String.format("%.1f", percentage) + "%)");
                
                // 显示内存变化
                MemorySnapshot startSnapshot = memorySnapshots.get(phase + "_start");
                MemorySnapshot endSnapshot = memorySnapshots.get(phase + "_end");
                if (startSnapshot != null && endSnapshot != null) {
                    long memoryIncrease = endSnapshot.usedHeap - startSnapshot.usedHeap;
                    logger.info("    内存变化: " + formatBytes(startSnapshot.usedHeap) + 
                        " -> " + formatBytes(endSnapshot.usedHeap) + 
                        " (增加: " + formatBytes(memoryIncrease) + ")");
                }
            });
        
        // 显示当前系统状态
        logger.info("当前系统状态:");
        MemorySnapshot currentSnapshot = takeMemorySnapshot();
        logger.info("  堆内存使用: " + formatBytes(currentSnapshot.usedHeap) + 
            " / " + formatBytes(currentSnapshot.maxHeap));
        logger.info("  非堆内存使用: " + formatBytes(currentSnapshot.usedNonHeap) + 
            " / " + formatBytes(currentSnapshot.maxNonHeap));
        logger.info("  活跃线程数: " + threadBean.getThreadCount());
        logger.info("  守护线程数: " + threadBean.getDaemonThreadCount());
        
        logger.info("=== 性能报告结束 ===");
    }
    
    /**
     * 获取性能建议
     */
    public void generateOptimizationSuggestions() {
        logger.info("=== 性能优化建议 ===");
        
        // 分析最耗时的阶段
        String slowestPhase = durations.entrySet().stream()
            .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
            .map(entry -> entry.getKey())
            .orElse(null);
        
        if (slowestPhase != null) {
            long slowestTime = durations.get(slowestPhase);
            logger.info("最耗时阶段: " + slowestPhase + " (" + slowestTime + "ms)");
            
            if (slowestTime > 5000) {
                logger.warn("建议优化 " + slowestPhase + " 阶段，耗时过长");
            }
        }
        
        // 分析内存使用
        MemorySnapshot currentSnapshot = takeMemorySnapshot();
        double heapUsagePercent = (double) currentSnapshot.usedHeap / currentSnapshot.maxHeap * 100;
        
        if (heapUsagePercent > 80) {
            logger.warn("堆内存使用率过高: " + String.format("%.1f", heapUsagePercent) + "%，建议增加堆内存大小");
        }
        
        // 分析启动时间
        long totalTime = getTotalStartupTime();
        if (totalTime > 30000) {
            logger.warn("总启动时间过长: " + totalTime + "ms，建议进行启动优化");
        } else if (totalTime > 0 && totalTime < 10000) {
            logger.info("启动时间良好: " + totalTime + "ms");
        }
        
        // 线程分析
        int threadCount = threadBean.getThreadCount();
        if (threadCount > 100) {
            logger.warn("线程数量较多: " + threadCount + "，建议检查线程池配置");
        }
        
        logger.info("=== 优化建议结束 ===");
    }
    
    /**
     * 拍摄内存快照
     */
    private MemorySnapshot takeMemorySnapshot() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        return new MemorySnapshot(
            heapUsage.getUsed(),
            heapUsage.getMax(),
            nonHeapUsage.getUsed(),
            nonHeapUsage.getMax(),
            System.currentTimeMillis()
        );
    }
    
    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 0) return "N/A";
        
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
     * 清理性能数据
     */
    public void cleanup() {
        startTimes.clear();
        endTimes.clear();
        durations.clear();
        memorySnapshots.clear();
        globalStartTime.set(0);
        logger.debug("性能监控数据已清理");
    }
    
    /**
     * 内存快照数据类
     */
    private static class MemorySnapshot {
        final long usedHeap;
        final long maxHeap;
        final long usedNonHeap;
        final long maxNonHeap;
        final long timestamp;
        
        MemorySnapshot(long usedHeap, long maxHeap, long usedNonHeap, long maxNonHeap, long timestamp) {
            this.usedHeap = usedHeap;
            this.maxHeap = maxHeap;
            this.usedNonHeap = usedNonHeap;
            this.maxNonHeap = maxNonHeap;
            this.timestamp = timestamp;
        }
    }
}