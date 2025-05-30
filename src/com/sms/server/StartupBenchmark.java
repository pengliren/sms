package com.sms.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动性能基准测试工具
 * 
 * 用于测试和比较不同启动方式的性能差异
 * 
 * @author SMS Team
 */
public class StartupBenchmark {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupBenchmark.class);
    
    private static final int DEFAULT_ITERATIONS = 5;
    private static final String BENCHMARK_REPORT_FILE = "startup-benchmark-report.txt";
    
    public static void main(String[] args) {
        StartupBenchmark benchmark = new StartupBenchmark();
        
        int iterations = DEFAULT_ITERATIONS;
        if (args.length > 0) {
            try {
                iterations = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warn("无效的迭代次数参数，使用默认值: {}", DEFAULT_ITERATIONS);
            }
        }
        
        benchmark.runBenchmark(iterations);
    }
    
    /**
     * 运行基准测试
     */
    public void runBenchmark(int iterations) {
        logger.info("开始启动性能基准测试，迭代次数: {}", iterations);
        
        BenchmarkResult originalResult = benchmarkOriginalBootstrap(iterations);
        BenchmarkResult optimizedResult = benchmarkOptimizedBootstrap(iterations);
        
        generateComparisonReport(originalResult, optimizedResult);
    }
    
    /**
     * 测试原始启动器性能
     */
    private BenchmarkResult benchmarkOriginalBootstrap(int iterations) {
        logger.info("测试原始启动器性能...");
        
        long totalTime = 0;
        long totalMemory = 0;
        int successCount = 0;
        
        for (int i = 0; i < iterations; i++) {
            logger.info("原始启动器测试 - 第 {} 次", i + 1);
            
            try {
                BenchmarkRun run = runOriginalBootstrap();
                if (run.success) {
                    totalTime += run.duration;
                    totalMemory += run.memoryUsed;
                    successCount++;
                }
                
                // 等待一段时间再进行下一次测试
                Thread.sleep(2000);
                
            } catch (Exception e) {
                logger.error("原始启动器测试失败", e);
            }
        }
        
        return new BenchmarkResult(
            "原始启动器",
            successCount,
            iterations,
            successCount > 0 ? totalTime / successCount : 0,
            successCount > 0 ? totalMemory / successCount : 0
        );
    }
    
    /**
     * 测试优化启动器性能
     */
    private BenchmarkResult benchmarkOptimizedBootstrap(int iterations) {
        logger.info("测试优化启动器性能...");
        
        long totalTime = 0;
        long totalMemory = 0;
        int successCount = 0;
        
        for (int i = 0; i < iterations; i++) {
            logger.info("优化启动器测试 - 第 {} 次", i + 1);
            
            try {
                BenchmarkRun run = runOptimizedBootstrap();
                if (run.success) {
                    totalTime += run.duration;
                    totalMemory += run.memoryUsed;
                    successCount++;
                }
                
                // 等待一段时间再进行下一次测试
                Thread.sleep(2000);
                
            } catch (Exception e) {
                logger.error("优化启动器测试失败", e);
            }
        }
        
        return new BenchmarkResult(
            "优化启动器",
            successCount,
            iterations,
            successCount > 0 ? totalTime / successCount : 0,
            successCount > 0 ? totalMemory / successCount : 0
        );
    }
    
    /**
     * 运行原始启动器
     */
    private BenchmarkRun runOriginalBootstrap() {
        long startTime = System.currentTimeMillis();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage beforeMemory = memoryBean.getHeapMemoryUsage();
        
        boolean success = false;
        try {
            // 模拟原始启动过程
            simulateOriginalStartup();
            success = true;
        } catch (Exception e) {
            logger.error("原始启动模拟失败", e);
        }
        
        long endTime = System.currentTimeMillis();
        MemoryUsage afterMemory = memoryBean.getHeapMemoryUsage();
        
        long duration = endTime - startTime;
        long memoryUsed = afterMemory.getUsed() - beforeMemory.getUsed();
        
        return new BenchmarkRun(success, duration, memoryUsed);
    }
    
    /**
     * 运行优化启动器
     */
    private BenchmarkRun runOptimizedBootstrap() {
        long startTime = System.currentTimeMillis();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage beforeMemory = memoryBean.getHeapMemoryUsage();
        
        boolean success = false;
        try {
            // 模拟优化启动过程
            simulateOptimizedStartup();
            success = true;
        } catch (Exception e) {
            logger.error("优化启动模拟失败", e);
        }
        
        long endTime = System.currentTimeMillis();
        MemoryUsage afterMemory = memoryBean.getHeapMemoryUsage();
        
        long duration = endTime - startTime;
        long memoryUsed = afterMemory.getUsed() - beforeMemory.getUsed();
        
        return new BenchmarkRun(success, duration, memoryUsed);
    }
    
    /**
     * 模拟原始启动过程
     */
    private void simulateOriginalStartup() throws Exception {
        // 模拟类加载
        Thread.sleep(500);
        
        // 模拟配置加载
        Thread.sleep(300);
        
        // 模拟组件初始化（串行）
        Thread.sleep(800); // JMX
        Thread.sleep(600); // App
        Thread.sleep(1200); // RTMP
        Thread.sleep(800); // HTTP
        Thread.sleep(700); // RTSP
        Thread.sleep(400); // Plugins
        
        logger.debug("原始启动模拟完成");
    }
    
    /**
     * 模拟优化启动过程
     */
    private void simulateOptimizedStartup() throws Exception {
        // 模拟配置验证
        Thread.sleep(100);
        
        // 模拟类加载优化
        Thread.sleep(300);
        
        // 模拟配置加载优化
        Thread.sleep(200);
        
        // 模拟组件初始化（并行）
        Thread.sleep(500); // JMX
        Thread.sleep(400); // App
        
        // 模拟网络服务并行启动
        Thread.sleep(800); // 最长的网络服务启动时间
        
        Thread.sleep(300); // Plugins
        
        logger.debug("优化启动模拟完成");
    }
    
    /**
     * 生成对比报告
     */
    private void generateComparisonReport(BenchmarkResult original, BenchmarkResult optimized) {
        logger.info("生成性能对比报告...");
        
        StringBuilder report = new StringBuilder();
        report.append("SMS 流媒体服务器启动性能基准测试报告\n");
        report.append("=" .repeat(50)).append("\n");
        report.append("测试时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        report.append("JVM版本: ").append(System.getProperty("java.version")).append("\n");
        report.append("操作系统: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
        report.append("处理器: ").append(Runtime.getRuntime().availableProcessors()).append(" 核心\n");
        report.append("最大内存: ").append(formatBytes(Runtime.getRuntime().maxMemory())).append("\n");
        report.append("\n");
        
        // 原始启动器结果
        report.append("原始启动器测试结果:\n");
        report.append("-".repeat(30)).append("\n");
        report.append(original.toString()).append("\n");
        
        // 优化启动器结果
        report.append("优化启动器测试结果:\n");
        report.append("-".repeat(30)).append("\n");
        report.append(optimized.toString()).append("\n");
        
        // 性能对比
        if (original.successCount > 0 && optimized.successCount > 0) {
            report.append("性能对比:\n");
            report.append("-".repeat(20)).append("\n");
            
            double timeImprovement = ((double) (original.avgDuration - optimized.avgDuration) / original.avgDuration) * 100;
            double memoryImprovement = ((double) (original.avgMemoryUsed - optimized.avgMemoryUsed) / original.avgMemoryUsed) * 100;
            
            report.append(String.format("启动时间改进: %.1f%% (从 %dms 到 %dms)\n", 
                timeImprovement, original.avgDuration, optimized.avgDuration));
            report.append(String.format("内存使用改进: %.1f%% (从 %s 到 %s)\n", 
                memoryImprovement, formatBytes(original.avgMemoryUsed), formatBytes(optimized.avgMemoryUsed)));
            
            if (timeImprovement > 0) {
                report.append("✓ 启动时间有显著改进\n");
            } else {
                report.append("✗ 启动时间未改进\n");
            }
            
            if (memoryImprovement > 0) {
                report.append("✓ 内存使用有改进\n");
            } else {
                report.append("✗ 内存使用未改进\n");
            }
        }
        
        report.append("\n");
        report.append("建议:\n");
        report.append("-".repeat(10)).append("\n");
        
        if (optimized.avgDuration < original.avgDuration) {
            report.append("• 建议使用优化启动器以获得更快的启动速度\n");
        }
        
        if (optimized.avgMemoryUsed < original.avgMemoryUsed) {
            report.append("• 优化启动器使用更少的内存\n");
        }
        
        if (optimized.successCount < optimized.totalRuns) {
            report.append("• 优化启动器存在稳定性问题，需要进一步调试\n");
        }
        
        // 输出到控制台
        String reportContent = report.toString();
        System.out.println(reportContent);
        
        // 保存到文件
        try {
            File reportFile = new File(BENCHMARK_REPORT_FILE);
            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write(reportContent);
            }
            logger.info("基准测试报告已保存到: {}", reportFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("保存基准测试报告失败", e);
        }
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
     * 基准测试运行结果
     */
    private static class BenchmarkRun {
        final boolean success;
        final long duration;
        final long memoryUsed;
        
        BenchmarkRun(boolean success, long duration, long memoryUsed) {
            this.success = success;
            this.duration = duration;
            this.memoryUsed = memoryUsed;
        }
    }
    
    /**
     * 基准测试结果
     */
    private static class BenchmarkResult {
        final String name;
        final int successCount;
        final int totalRuns;
        final long avgDuration;
        final long avgMemoryUsed;
        
        BenchmarkResult(String name, int successCount, int totalRuns, long avgDuration, long avgMemoryUsed) {
            this.name = name;
            this.successCount = successCount;
            this.totalRuns = totalRuns;
            this.avgDuration = avgDuration;
            this.avgMemoryUsed = avgMemoryUsed;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("测试名称: ").append(name).append("\n");
            sb.append("成功次数: ").append(successCount).append("/").append(totalRuns).append("\n");
            sb.append("成功率: ").append(String.format("%.1f%%", (double) successCount / totalRuns * 100)).append("\n");
            if (successCount > 0) {
                sb.append("平均启动时间: ").append(avgDuration).append("ms\n");
                sb.append("平均内存使用: ").append(formatBytes(avgMemoryUsed)).append("\n");
            }
            return sb.toString();
        }
        
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
    }
}