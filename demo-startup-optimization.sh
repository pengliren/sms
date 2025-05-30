#!/bin/bash

# SMS 流媒体服务器启动优化演示脚本
# 演示原始启动器与优化启动器的性能差异

echo "=========================================="
echo "SMS 流媒体服务器启动优化演示"
echo "=========================================="
echo

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java环境，请先安装Java 8或更高版本"
    exit 1
fi

echo "✅ Java环境检查通过"
java -version
echo

# 编译所有优化类（如果需要）
echo "📦 编译优化类..."
if [ ! -d "bin" ]; then
    mkdir bin
fi

javac -cp "lib/*" -d bin src/com/sms/classloading/ClassLoaderBuilder.java 2>/dev/null
javac -cp "lib/*:bin" -d bin src/com/sms/server/StartupProfiler.java src/com/sms/server/ConfigValidator.java src/com/sms/server/StartupBenchmark.java src/com/sms/server/OptimizedBootstrap.java 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ 编译成功"
else
    echo "❌ 编译失败，请检查Java环境和依赖"
    exit 1
fi
echo

# 运行性能基准测试
echo "🚀 运行启动性能基准测试..."
echo "这将比较原始启动器和优化启动器的性能差异"
echo "测试可能需要几分钟时间，请耐心等待..."
echo

java -cp "lib/*:bin" com.sms.server.StartupBenchmark

echo
echo "📊 基准测试完成！"
echo

# 显示测试报告
if [ -f "startup-benchmark-report.txt" ]; then
    echo "📋 测试报告摘要:"
    echo "----------------------------------------"
    grep -E "(平均启动时间|启动时间改进|建议)" startup-benchmark-report.txt
    echo "----------------------------------------"
    echo
    echo "📄 完整报告已保存到: startup-benchmark-report.txt"
else
    echo "❌ 未找到测试报告文件"
fi

echo
echo "🎯 优化要点:"
echo "• 并行启动网络服务 (RTMP, HTTP, RTSP)"
echo "• 配置预验证避免启动失败"
echo "• 性能监控和内存跟踪"
echo "• JVM参数优化"
echo "• 健康检查和优雅关闭"
echo

echo "📚 使用说明:"
echo "• 使用优化启动器: ./startup-optimized.sh start"
echo "• 查看详细文档: cat STARTUP_OPTIMIZATION.md"
echo "• 运行配置验证: java -cp \"lib/*:bin\" com.sms.server.ConfigValidator"
echo

echo "✨ 演示完成！感谢使用SMS流媒体服务器启动优化方案。"