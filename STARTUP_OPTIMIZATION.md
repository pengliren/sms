# SMS 流媒体服务器启动优化

本文档介绍了SMS流媒体服务器的启动优化方案，包括优化的启动器、性能监控工具和配置验证器。

## 🚀 优化概述

### 主要改进点

1. **并行启动**: 网络服务(RTMP/HTTP/RTSP)并行启动，减少总启动时间
2. **配置验证**: 启动前验证配置文件，提前发现问题
3. **性能监控**: 实时监控启动过程中的性能指标
4. **错误处理**: 更好的异常处理和错误恢复机制
5. **健康检查**: 启动后持续监控系统健康状态
6. **优雅关闭**: 改进的关闭流程，确保资源正确释放

### 性能提升预期

- **启动时间**: 减少30-50%
- **内存使用**: 优化10-20%
- **稳定性**: 显著提升
- **可维护性**: 大幅改善

## 📁 新增文件

```
src/com/sms/server/
├── OptimizedBootstrap.java     # 优化的启动器
├── StartupProfiler.java        # 启动性能分析器
├── ConfigValidator.java        # 配置验证器
└── StartupBenchmark.java       # 性能基准测试工具

startup-optimized.sh            # Linux/macOS优化启动脚本
startup-optimized.bat           # Windows优化启动脚本
```

## 🔧 使用方法

### 1. 使用优化启动脚本 (推荐)

#### Linux/macOS

```bash
# 基本启动
./startup-optimized.sh start

# 后台启动
./startup-optimized.sh -d start

# 自定义堆内存
./startup-optimized.sh -h 2g start

# 启用JMX监控
./startup-optimized.sh -j -p 9999 start

# 调试模式
./startup-optimized.sh --debug start

# 查看状态
./startup-optimized.sh status

# 停止服务器
./startup-optimized.sh stop

# 重启服务器
./startup-optimized.sh restart
```

#### Windows

```cmd
REM 基本启动
startup-optimized.bat start

REM 后台启动
startup-optimized.bat /d start

REM 自定义堆内存
startup-optimized.bat /h 2g start

REM 启用JMX监控
startup-optimized.bat /j /p 9999 start

REM 查看帮助
startup-optimized.bat /?
```

### 2. 直接使用优化启动器

```bash
# 编译优化启动器
javac -cp "lib/*" src/com/sms/server/OptimizedBootstrap.java

# 使用优化启动器
java -cp "lib/*:src" com.sms.server.OptimizedBootstrap start
```

### 3. 环境变量配置

```bash
# 设置SMS安装目录
export SMS_HOME=/path/to/sms

# 设置Java目录
export JAVA_HOME=/path/to/java

# 设置堆内存大小
export HEAP_SIZE=2g

# 自定义JVM参数
export CUSTOM_JVM_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启用调试模式
export DEBUG=true

# 启用JMX监控
export ENABLE_JMX=true
export JMX_PORT=9999
```

## 📊 性能监控

### 启动性能分析

优化启动器会自动生成启动性能报告：

```
=== 启动性能报告 ===
总启动时间: 8500ms
各阶段性能数据:
  初始化: 1200ms (14.1%)
    内存变化: 45.2MB -> 52.1MB (增加: 6.9MB)
  类加载器: 800ms (9.4%)
    内存变化: 52.1MB -> 58.3MB (增加: 6.2MB)
  日志系统: 300ms (3.5%)
    内存变化: 58.3MB -> 60.1MB (增加: 1.8MB)
  配置系统: 500ms (5.9%)
    内存变化: 60.1MB -> 65.4MB (增加: 5.3MB)
  JMX代理: 600ms (7.1%)
    内存变化: 65.4MB -> 68.2MB (增加: 2.8MB)
  应用加载: 1500ms (17.6%)
    内存变化: 68.2MB -> 85.6MB (增加: 17.4MB)
  网络服务: 3200ms (37.6%)
    内存变化: 85.6MB -> 125.3MB (增加: 39.7MB)
  插件系统: 400ms (4.7%)
    内存变化: 125.3MB -> 128.1MB (增加: 2.8MB)
```

### 优化建议

系统会自动分析性能数据并提供优化建议：

```
=== 性能优化建议 ===
最耗时阶段: 网络服务 (3200ms)
建议优化 网络服务 阶段，耗时过长
堆内存使用率: 65.2%，使用率正常
启动时间良好: 8500ms
线程数量: 45，线程配置合理
```

## 🔍 配置验证

### 自动配置检查

启动前会自动验证配置文件：

```
[INFO] 开始验证服务器配置...
[INFO] 环境验证通过
[WARN] 配置验证发现警告:
  [警告] HTTP工作线程数配置可能不合适: 64
  [警告] RTMP服务器带宽配置可能不合适: 10000000
[INFO] 配置验证通过
```

### 手动配置验证

```java
// 单独运行配置验证
ConfigValidator validator = new ConfigValidator();
ConfigValidator.ValidationResult result = validator.validateConfiguration("/path/to/conf");

if (!result.isValid()) {
    System.err.println("配置验证失败:");
    for (String error : result.getErrors()) {
        System.err.println("  " + error);
    }
}
```

## 🧪 性能基准测试

### 运行基准测试

```bash
# 编译基准测试工具
javac -cp "lib/*" src/com/sms/server/StartupBenchmark.java

# 运行基准测试（默认5次迭代）
java -cp "lib/*:src" com.sms.server.StartupBenchmark

# 自定义迭代次数
java -cp "lib/*:src" com.sms.server.StartupBenchmark 10
```

### 基准测试报告

```
SMS 流媒体服务器启动性能基准测试报告
==================================================
测试时间: 2024-01-15 10:30:45
JVM版本: 11.0.16
操作系统: Linux 5.4.0
处理器: 8 核心
最大内存: 2.00 GB

原始启动器测试结果:
------------------------------
测试名称: 原始启动器
成功次数: 5/5
成功率: 100.0%
平均启动时间: 12500ms
平均内存使用: 156.30 MB

优化启动器测试结果:
------------------------------
测试名称: 优化启动器
成功次数: 5/5
成功率: 100.0%
平均启动时间: 8200ms
平均内存使用: 142.80 MB

性能对比:
--------------------
启动时间改进: 34.4% (从 12500ms 到 8200ms)
内存使用改进: 8.6% (从 156.30 MB 到 142.80 MB)
✓ 启动时间有显著改进
✓ 内存使用有改进

建议:
----------
• 建议使用优化启动器以获得更快的启动速度
• 优化启动器使用更少的内存
```

## ⚙️ 高级配置

### JVM参数优化

启动脚本会根据系统配置自动优化JVM参数：

#### Java 8
```bash
-XX:+UseConcMarkSweepGC
-XX:+CMSParallelRemarkEnabled
-XX:+UseCMSInitiatingOccupancyOnly
-XX:CMSInitiatingOccupancyFraction=70
```

#### Java 11+
```bash
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
```

#### 通用优化
```bash
-XX:+UseCompressedOops
-XX:+UseCompressedClassPointers
-XX:+OptimizeStringConcat
-XX:+UseStringDeduplication
-Djava.net.preferIPv4Stack=true
-Djava.awt.headless=true
-Dfile.encoding=UTF-8
```

### 内存配置建议

| 系统内存 | 推荐堆大小 | JVM参数 |
|---------|-----------|---------|
| < 2GB   | 512MB     | -Xms512m -Xmx512m |
| 2-4GB   | 1GB       | -Xms1g -Xmx1g |
| 4-8GB   | 2GB       | -Xms2g -Xmx2g |
| > 8GB   | 4GB       | -Xms4g -Xmx4g |

### 网络优化配置

在 `conf/server.properties` 中优化网络参数：

```properties
# HTTP优化
http.io_threads=4
http.worker_threads=32
http.send_buffer_size=32768
http.receive_buffer_size=32768

# RTMP优化
rtmp.io_threads=8
rtmp.worker_threads=64
rtmp.send_buffer_size=131072
rtmp.receive_buffer_size=131072

# RTSP优化
rtsp.io_threads=4
rtsp.worker_threads=32
```

## 🐛 故障排除

### 常见问题

1. **启动失败**
   ```
   [ERROR] 配置验证失败:
     [错误] HTTP端口配置无效: abc
   ```
   **解决**: 检查 `conf/server.properties` 中的端口配置

2. **内存不足**
   ```
   [WARN] 系统内存较少 (1024MB)，建议至少1GB内存
   ```
   **解决**: 增加系统内存或减少堆内存大小

3. **端口占用**
   ```
   [WARN] 端口 1935 已被占用
   ```
   **解决**: 停止占用端口的进程或修改配置文件中的端口

4. **Java版本过低**
   ```
   [ERROR] Java版本过低: 1.7.0_80，需要Java 8或更高版本
   ```
   **解决**: 升级Java版本或设置正确的JAVA_HOME

### 调试模式

启用调试模式获取更多信息：

```bash
# Linux/macOS
./startup-optimized.sh --debug start

# Windows
startup-optimized.bat /debug start

# 或设置环境变量
export DEBUG=true
```

### 日志文件

- **启动日志**: `logs/startup.log`
- **GC日志**: `logs/gc.log` (调试模式)
- **应用日志**: `logs/sms.log`

## 📈 监控和维护

### JMX监控

启用JMX监控：

```bash
./startup-optimized.sh -j -p 9999 start
```

使用JConsole或VisualVM连接：
```
service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi
```

### 健康检查

优化启动器会自动进行健康检查：

- **内存使用监控**: 每30秒检查一次
- **线程状态监控**: 检测死锁和异常线程
- **系统资源监控**: CPU和磁盘使用情况

### 性能调优建议

1. **定期运行基准测试**，监控性能变化
2. **根据负载调整线程池大小**
3. **监控GC日志**，优化垃圾收集参数
4. **使用JMX监控**运行时性能指标
5. **定期更新Java版本**获得最新优化

## 🔄 迁移指南

### 从原始启动器迁移

1. **备份现有配置**
   ```bash
   cp -r conf conf.backup
   ```

2. **测试优化启动器**
   ```bash
   ./startup-optimized.sh start
   ```

3. **验证功能正常**
   - 检查所有服务是否正常启动
   - 验证RTMP/HTTP/RTSP连接
   - 测试流媒体播放功能

4. **更新启动脚本**
   ```bash
   # 替换原有启动脚本
   mv startup.sh startup.sh.old
   ln -s startup-optimized.sh startup.sh
   ```

### 回滚方案

如果遇到问题，可以快速回滚：

```bash
# 停止优化启动器
./startup-optimized.sh stop

# 使用原始启动器
java -jar boot.jar start
```

## 📞 支持

如果在使用过程中遇到问题：

1. 查看本文档的故障排除部分
2. 检查日志文件获取详细错误信息
3. 运行配置验证器检查配置问题
4. 使用调试模式获取更多信息

---

**注意**: 建议在生产环境使用前，先在测试环境充分验证优化启动器的稳定性和性能。