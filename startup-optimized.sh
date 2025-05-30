#!/bin/bash

# SMS 流媒体服务器优化启动脚本
# 
# 主要优化点：
# 1. JVM参数优化
# 2. 启动前环境检查
# 3. 性能监控
# 4. 错误处理和恢复

# 设置脚本错误时退出
set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_debug() {
    if [ "$DEBUG" = "true" ]; then
        echo -e "${BLUE}[DEBUG]${NC} $1"
    fi
}

# 检查是否为root用户
check_root() {
    if [ "$EUID" -eq 0 ]; then
        log_warn "不建议以root用户运行SMS服务器"
        read -p "是否继续? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# 检查Java环境
check_java() {
    log_info "检查Java环境..."
    
    if [ -z "$JAVA_HOME" ]; then
        log_warn "JAVA_HOME未设置，尝试查找Java..."
        JAVA_CMD=$(which java 2>/dev/null || echo "")
        if [ -z "$JAVA_CMD" ]; then
            log_error "未找到Java，请安装Java 8或更高版本"
            exit 1
        fi
    else
        JAVA_CMD="$JAVA_HOME/bin/java"
        if [ ! -x "$JAVA_CMD" ]; then
            log_error "Java可执行文件不存在: $JAVA_CMD"
            exit 1
        fi
    fi
    
    # 检查Java版本
    JAVA_VERSION=$($JAVA_CMD -version 2>&1 | head -n 1 | cut -d'"' -f2)
    JAVA_MAJOR_VERSION=$(echo $JAVA_VERSION | cut -d'.' -f1)
    if [ "$JAVA_MAJOR_VERSION" -lt 8 ]; then
        log_error "Java版本过低: $JAVA_VERSION，需要Java 8或更高版本"
        exit 1
    fi
    
    log_info "Java版本: $JAVA_VERSION"
}

# 检查系统资源
check_system_resources() {
    log_info "检查系统资源..."
    
    # 检查内存
    TOTAL_MEM=$(free -m | awk 'NR==2{printf "%.0f", $2}')
    AVAILABLE_MEM=$(free -m | awk 'NR==2{printf "%.0f", $7}')
    
    log_info "总内存: ${TOTAL_MEM}MB, 可用内存: ${AVAILABLE_MEM}MB"
    
    if [ "$TOTAL_MEM" -lt 1024 ]; then
        log_warn "系统内存较少 (${TOTAL_MEM}MB)，建议至少1GB内存"
    fi
    
    if [ "$AVAILABLE_MEM" -lt 512 ]; then
        log_warn "可用内存不足 (${AVAILABLE_MEM}MB)，可能影响性能"
    fi
    
    # 检查磁盘空间
    DISK_USAGE=$(df -h . | awk 'NR==2 {print $5}' | sed 's/%//')
    if [ "$DISK_USAGE" -gt 90 ]; then
        log_warn "磁盘使用率过高: ${DISK_USAGE}%"
    fi
    
    # 检查CPU核心数
    CPU_CORES=$(nproc)
    log_info "CPU核心数: $CPU_CORES"
}

# 设置SMS_HOME
setup_sms_home() {
    if [ -z "$SMS_HOME" ]; then
        SMS_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
        export SMS_HOME
    fi
    
    log_info "SMS_HOME: $SMS_HOME"
    
    if [ ! -d "$SMS_HOME" ]; then
        log_error "SMS_HOME目录不存在: $SMS_HOME"
        exit 1
    fi
    
    # 检查必要的文件和目录
    for item in "boot.jar" "conf" "lib"; do
        if [ ! -e "$SMS_HOME/$item" ]; then
            log_error "必要的文件/目录不存在: $SMS_HOME/$item"
            exit 1
        fi
    done
}

# 优化JVM参数
setup_jvm_options() {
    log_info "设置JVM参数..."
    
    # 基础内存设置
    if [ -z "$HEAP_SIZE" ]; then
        # 根据系统内存自动设置堆大小
        if [ "$TOTAL_MEM" -gt 4096 ]; then
            HEAP_SIZE="2g"
        elif [ "$TOTAL_MEM" -gt 2048 ]; then
            HEAP_SIZE="1g"
        else
            HEAP_SIZE="512m"
        fi
    fi
    
    log_info "堆内存大小: $HEAP_SIZE"
    
    # JVM参数
    JVM_OPTS="-Xms$HEAP_SIZE -Xmx$HEAP_SIZE"
    
    # 垃圾收集器优化
    if [ "$JAVA_MAJOR_VERSION" -ge 11 ]; then
        # Java 11+ 使用G1GC
        JVM_OPTS="$JVM_OPTS -XX:+UseG1GC"
        JVM_OPTS="$JVM_OPTS -XX:MaxGCPauseMillis=200"
        JVM_OPTS="$JVM_OPTS -XX:G1HeapRegionSize=16m"
    else
        # Java 8 使用CMS
        JVM_OPTS="$JVM_OPTS -XX:+UseConcMarkSweepGC"
        JVM_OPTS="$JVM_OPTS -XX:+CMSParallelRemarkEnabled"
        JVM_OPTS="$JVM_OPTS -XX:+UseCMSInitiatingOccupancyOnly"
        JVM_OPTS="$JVM_OPTS -XX:CMSInitiatingOccupancyFraction=70"
    fi
    
    # 性能优化参数
    JVM_OPTS="$JVM_OPTS -XX:+UseCompressedOops"
    JVM_OPTS="$JVM_OPTS -XX:+UseCompressedClassPointers"
    JVM_OPTS="$JVM_OPTS -XX:+OptimizeStringConcat"
    JVM_OPTS="$JVM_OPTS -XX:+UseStringDeduplication"
    
    # 网络优化
    JVM_OPTS="$JVM_OPTS -Djava.net.preferIPv4Stack=true"
    JVM_OPTS="$JVM_OPTS -Djava.awt.headless=true"
    
    # 文件编码
    JVM_OPTS="$JVM_OPTS -Dfile.encoding=UTF-8"
    
    # 系统属性
    JVM_OPTS="$JVM_OPTS -Dsms.root=$SMS_HOME"
    JVM_OPTS="$JVM_OPTS -Dsms.config_root=$SMS_HOME/conf"
    
    # 调试和监控（可选）
    if [ "$DEBUG" = "true" ]; then
        JVM_OPTS="$JVM_OPTS -XX:+PrintGC"
        JVM_OPTS="$JVM_OPTS -XX:+PrintGCDetails"
        JVM_OPTS="$JVM_OPTS -XX:+PrintGCTimeStamps"
        JVM_OPTS="$JVM_OPTS -Xloggc:$SMS_HOME/logs/gc.log"
    fi
    
    # JMX监控（如果启用）
    if [ "$ENABLE_JMX" = "true" ]; then
        JMX_PORT=${JMX_PORT:-9999}
        JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote"
        JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.port=$JMX_PORT"
        JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
        JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.ssl=false"
        log_info "JMX监控端口: $JMX_PORT"
    fi
    
    # 用户自定义JVM参数
    if [ -n "$CUSTOM_JVM_OPTS" ]; then
        JVM_OPTS="$JVM_OPTS $CUSTOM_JVM_OPTS"
    fi
    
    log_debug "JVM参数: $JVM_OPTS"
}

# 检查端口占用
check_ports() {
    log_info "检查端口占用..."
    
    # 从配置文件读取端口
    CONFIG_FILE="$SMS_HOME/conf/server.properties"
    if [ -f "$CONFIG_FILE" ]; then
        HTTP_PORT=$(grep "^http.port=" "$CONFIG_FILE" | cut -d'=' -f2 | tr -d ' ')
        RTMP_PORT=$(grep "^rtmp.port=" "$CONFIG_FILE" | cut -d'=' -f2 | tr -d ' ')
        RTSP_PORT=$(grep "^rtsp.port=" "$CONFIG_FILE" | cut -d'=' -f2 | tr -d ' ')
        
        for port in $HTTP_PORT $RTMP_PORT $RTSP_PORT; do
            if [ -n "$port" ] && netstat -ln | grep -q ":$port "; then
                log_warn "端口 $port 已被占用"
            fi
        done
    fi
}

# 创建必要的目录
create_directories() {
    log_info "创建必要的目录..."
    
    for dir in "logs" "webapps/root/streams" "temp"; do
        if [ ! -d "$SMS_HOME/$dir" ]; then
            mkdir -p "$SMS_HOME/$dir"
            log_debug "创建目录: $SMS_HOME/$dir"
        fi
    done
}

# 启动服务器
start_server() {
    log_info "启动SMS流媒体服务器..."
    
    # 切换到SMS_HOME目录
    cd "$SMS_HOME"
    
    # 构建启动命令
    START_CMD="$JAVA_CMD $JVM_OPTS -jar boot.jar start"
    
    log_debug "启动命令: $START_CMD"
    
    # 启动服务器
    if [ "$DAEMON" = "true" ]; then
        # 后台运行
        nohup $START_CMD > logs/startup.log 2>&1 &
        PID=$!
        echo $PID > logs/sms.pid
        log_info "服务器已在后台启动，PID: $PID"
        log_info "日志文件: $SMS_HOME/logs/startup.log"
    else
        # 前台运行
        exec $START_CMD
    fi
}

# 停止服务器
stop_server() {
    log_info "停止SMS流媒体服务器..."
    
    PID_FILE="$SMS_HOME/logs/sms.pid"
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            log_info "正在停止服务器 (PID: $PID)..."
            kill "$PID"
            
            # 等待进程结束
            for i in {1..30}; do
                if ! kill -0 "$PID" 2>/dev/null; then
                    log_info "服务器已停止"
                    rm -f "$PID_FILE"
                    return 0
                fi
                sleep 1
            done
            
            # 强制杀死
            log_warn "强制停止服务器..."
            kill -9 "$PID" 2>/dev/null || true
            rm -f "$PID_FILE"
        else
            log_warn "PID文件存在但进程不存在，清理PID文件"
            rm -f "$PID_FILE"
        fi
    else
        log_warn "未找到PID文件，尝试通过Java进程名停止..."
        pkill -f "boot.jar" || log_warn "未找到运行中的SMS服务器进程"
    fi
}

# 显示服务器状态
show_status() {
    PID_FILE="$SMS_HOME/logs/sms.pid"
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            log_info "SMS服务器正在运行 (PID: $PID)"
            
            # 显示内存使用情况
            if command -v ps >/dev/null 2>&1; then
                MEM_USAGE=$(ps -p "$PID" -o rss= 2>/dev/null | awk '{print int($1/1024)}')
                if [ -n "$MEM_USAGE" ]; then
                    log_info "内存使用: ${MEM_USAGE}MB"
                fi
            fi
            
            return 0
        else
            log_warn "PID文件存在但进程不存在"
            rm -f "$PID_FILE"
        fi
    fi
    
    log_info "SMS服务器未运行"
    return 1
}

# 显示帮助信息
show_help() {
    echo "SMS 流媒体服务器启动脚本"
    echo ""
    echo "用法: $0 [选项] [命令]"
    echo ""
    echo "命令:"
    echo "  start    启动服务器"
    echo "  stop     停止服务器"
    echo "  restart  重启服务器"
    echo "  status   显示服务器状态"
    echo "  help     显示此帮助信息"
    echo ""
    echo "选项:"
    echo "  -d, --daemon     后台运行"
    echo "  -h, --heap SIZE  设置堆内存大小 (例如: 1g, 512m)"
    echo "  -j, --jmx        启用JMX监控"
    echo "  -p, --jmx-port   JMX端口 (默认: 9999)"
    echo "  --debug          启用调试模式"
    echo "  --help           显示此帮助信息"
    echo ""
    echo "环境变量:"
    echo "  SMS_HOME         SMS安装目录"
    echo "  JAVA_HOME        Java安装目录"
    echo "  HEAP_SIZE        堆内存大小"
    echo "  CUSTOM_JVM_OPTS  自定义JVM参数"
    echo ""
    echo "示例:"
    echo "  $0 start                    # 启动服务器"
    echo "  $0 -d start                 # 后台启动服务器"
    echo "  $0 -h 2g start              # 使用2GB堆内存启动"
    echo "  $0 -j -p 9999 start         # 启用JMX监控"
}

# 主函数
main() {
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -d|--daemon)
                DAEMON="true"
                shift
                ;;
            -h|--heap)
                HEAP_SIZE="$2"
                shift 2
                ;;
            -j|--jmx)
                ENABLE_JMX="true"
                shift
                ;;
            -p|--jmx-port)
                JMX_PORT="$2"
                shift 2
                ;;
            --debug)
                DEBUG="true"
                shift
                ;;
            --help)
                show_help
                exit 0
                ;;
            start|stop|restart|status|help)
                COMMAND="$1"
                shift
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 默认命令
    if [ -z "$COMMAND" ]; then
        COMMAND="start"
    fi
    
    # 设置默认值
    DAEMON=${DAEMON:-false}
    DEBUG=${DEBUG:-false}
    ENABLE_JMX=${ENABLE_JMX:-false}
    
    # 执行命令
    case $COMMAND in
        start)
            check_root
            setup_sms_home
            check_java
            check_system_resources
            setup_jvm_options
            check_ports
            create_directories
            start_server
            ;;
        stop)
            setup_sms_home
            stop_server
            ;;
        restart)
            setup_sms_home
            stop_server
            sleep 2
            check_root
            check_java
            check_system_resources
            setup_jvm_options
            check_ports
            create_directories
            start_server
            ;;
        status)
            setup_sms_home
            show_status
            ;;
        help)
            show_help
            ;;
        *)
            log_error "未知命令: $COMMAND"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"