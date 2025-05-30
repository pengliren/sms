@echo off
setlocal enabledelayedexpansion

REM SMS 流媒体服务器优化启动脚本 (Windows版本)
REM 
REM 主要优化点：
REM 1. JVM参数优化
REM 2. 启动前环境检查
REM 3. 性能监控
REM 4. 错误处理和恢复

title SMS 流媒体服务器

REM 设置颜色代码
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM 日志函数
:log_info
echo %GREEN%[INFO]%NC% %~1
goto :eof

:log_warn
echo %YELLOW%[WARN]%NC% %~1
goto :eof

:log_error
echo %RED%[ERROR]%NC% %~1
goto :eof

:log_debug
if "%DEBUG%"=="true" (
    echo %BLUE%[DEBUG]%NC% %~1
)
goto :eof

REM 检查管理员权限
:check_admin
net session >nul 2>&1
if %errorLevel% == 0 (
    call :log_warn "检测到管理员权限，不建议以管理员身份运行SMS服务器"
    set /p "continue=是否继续? (y/N): "
    if /i not "!continue!"=="y" exit /b 1
)
goto :eof

REM 检查Java环境
:check_java
call :log_info "检查Java环境..."

if "%JAVA_HOME%"=="" (
    call :log_warn "JAVA_HOME未设置，尝试查找Java..."
    where java >nul 2>&1
    if errorlevel 1 (
        call :log_error "未找到Java，请安装Java 8或更高版本"
        exit /b 1
    )
    set "JAVA_CMD=java"
) else (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    if not exist "!JAVA_CMD!" (
        call :log_error "Java可执行文件不存在: !JAVA_CMD!"
        exit /b 1
    )
)

REM 检查Java版本
for /f "tokens=3" %%g in ('!JAVA_CMD! -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
    set JAVA_VERSION=!JAVA_VERSION:"=!
)

for /f "tokens=1,2 delims=." %%a in ("!JAVA_VERSION!") do (
    set JAVA_MAJOR=%%a
    if %%a==1 set JAVA_MAJOR=%%b
)

if !JAVA_MAJOR! LSS 8 (
    call :log_error "Java版本过低: !JAVA_VERSION!，需要Java 8或更高版本"
    exit /b 1
)

call :log_info "Java版本: !JAVA_VERSION!"
goto :eof

REM 检查系统资源
:check_system_resources
call :log_info "检查系统资源..."

REM 检查内存
for /f "skip=1" %%p in ('wmic computersystem get TotalPhysicalMemory') do (
    if not "%%p"=="" (
        set /a TOTAL_MEM=%%p/1024/1024
        goto :memory_done
    )
)
:memory_done

call :log_info "总内存: !TOTAL_MEM!MB"

if !TOTAL_MEM! LSS 1024 (
    call :log_warn "系统内存较少 (!TOTAL_MEM!MB)，建议至少1GB内存"
)

REM 检查磁盘空间
for /f "tokens=3" %%a in ('dir /-c ^| find "bytes free"') do set DISK_FREE=%%a
set /a DISK_FREE_GB=!DISK_FREE!/1024/1024/1024

if !DISK_FREE_GB! LSS 1 (
    call :log_warn "磁盘可用空间不足: !DISK_FREE_GB!GB"
)

REM 检查CPU核心数
for /f %%i in ('echo %NUMBER_OF_PROCESSORS%') do set CPU_CORES=%%i
call :log_info "CPU核心数: !CPU_CORES!"
goto :eof

REM 设置SMS_HOME
:setup_sms_home
if "%SMS_HOME%"=="" (
    set "SMS_HOME=%~dp0"
    set "SMS_HOME=!SMS_HOME:~0,-1!"
)

call :log_info "SMS_HOME: !SMS_HOME!"

if not exist "!SMS_HOME!" (
    call :log_error "SMS_HOME目录不存在: !SMS_HOME!"
    exit /b 1
)

REM 检查必要的文件和目录
for %%i in (boot.jar conf lib) do (
    if not exist "!SMS_HOME!\%%i" (
        call :log_error "必要的文件/目录不存在: !SMS_HOME!\%%i"
        exit /b 1
    )
)
goto :eof

REM 优化JVM参数
:setup_jvm_options
call :log_info "设置JVM参数..."

REM 基础内存设置
if "%HEAP_SIZE%"=="" (
    if !TOTAL_MEM! GTR 4096 (
        set "HEAP_SIZE=2g"
    ) else if !TOTAL_MEM! GTR 2048 (
        set "HEAP_SIZE=1g"
    ) else (
        set "HEAP_SIZE=512m"
    )
)

call :log_info "堆内存大小: !HEAP_SIZE!"

REM JVM参数
set "JVM_OPTS=-Xms!HEAP_SIZE! -Xmx!HEAP_SIZE!"

REM 垃圾收集器优化
if !JAVA_MAJOR! GEQ 11 (
    REM Java 11+ 使用G1GC
    set "JVM_OPTS=!JVM_OPTS! -XX:+UseG1GC"
    set "JVM_OPTS=!JVM_OPTS! -XX:MaxGCPauseMillis=200"
    set "JVM_OPTS=!JVM_OPTS! -XX:G1HeapRegionSize=16m"
) else (
    REM Java 8 使用CMS
    set "JVM_OPTS=!JVM_OPTS! -XX:+UseConcMarkSweepGC"
    set "JVM_OPTS=!JVM_OPTS! -XX:+CMSParallelRemarkEnabled"
    set "JVM_OPTS=!JVM_OPTS! -XX:+UseCMSInitiatingOccupancyOnly"
    set "JVM_OPTS=!JVM_OPTS! -XX:CMSInitiatingOccupancyFraction=70"
)

REM 性能优化参数
set "JVM_OPTS=!JVM_OPTS! -XX:+UseCompressedOops"
set "JVM_OPTS=!JVM_OPTS! -XX:+UseCompressedClassPointers"
set "JVM_OPTS=!JVM_OPTS! -XX:+OptimizeStringConcat"

REM 网络优化
set "JVM_OPTS=!JVM_OPTS! -Djava.net.preferIPv4Stack=true"
set "JVM_OPTS=!JVM_OPTS! -Djava.awt.headless=true"

REM 文件编码
set "JVM_OPTS=!JVM_OPTS! -Dfile.encoding=UTF-8"

REM 系统属性
set "JVM_OPTS=!JVM_OPTS! -Dsms.root=!SMS_HOME!"
set "JVM_OPTS=!JVM_OPTS! -Dsms.config_root=!SMS_HOME!\conf"

REM 调试和监控（可选）
if "%DEBUG%"=="true" (
    set "JVM_OPTS=!JVM_OPTS! -XX:+PrintGC"
    set "JVM_OPTS=!JVM_OPTS! -XX:+PrintGCDetails"
    set "JVM_OPTS=!JVM_OPTS! -XX:+PrintGCTimeStamps"
    set "JVM_OPTS=!JVM_OPTS! -Xloggc:!SMS_HOME!\logs\gc.log"
)

REM JMX监控（如果启用）
if "%ENABLE_JMX%"=="true" (
    if "%JMX_PORT%"=="" set "JMX_PORT=9999"
    set "JVM_OPTS=!JVM_OPTS! -Dcom.sun.management.jmxremote"
    set "JVM_OPTS=!JVM_OPTS! -Dcom.sun.management.jmxremote.port=!JMX_PORT!"
    set "JVM_OPTS=!JVM_OPTS! -Dcom.sun.management.jmxremote.authenticate=false"
    set "JVM_OPTS=!JVM_OPTS! -Dcom.sun.management.jmxremote.ssl=false"
    call :log_info "JMX监控端口: !JMX_PORT!"
)

REM 用户自定义JVM参数
if not "%CUSTOM_JVM_OPTS%"=="" (
    set "JVM_OPTS=!JVM_OPTS! %CUSTOM_JVM_OPTS%"
)

call :log_debug "JVM参数: !JVM_OPTS!"
goto :eof

REM 检查端口占用
:check_ports
call :log_info "检查端口占用..."

set "CONFIG_FILE=!SMS_HOME!\conf\server.properties"
if exist "!CONFIG_FILE!" (
    for /f "tokens=2 delims==" %%a in ('findstr "^http.port=" "!CONFIG_FILE!"') do set HTTP_PORT=%%a
    for /f "tokens=2 delims==" %%a in ('findstr "^rtmp.port=" "!CONFIG_FILE!"') do set RTMP_PORT=%%a
    for /f "tokens=2 delims==" %%a in ('findstr "^rtsp.port=" "!CONFIG_FILE!"') do set RTSP_PORT=%%a
    
    for %%p in (!HTTP_PORT! !RTMP_PORT! !RTSP_PORT!) do (
        if not "%%p"=="" (
            netstat -an | findstr ":%%p " >nul 2>&1
            if not errorlevel 1 (
                call :log_warn "端口 %%p 已被占用"
            )
        )
    )
)
goto :eof

REM 创建必要的目录
:create_directories
call :log_info "创建必要的目录..."

for %%d in (logs webapps\root\streams temp) do (
    if not exist "!SMS_HOME!\%%d" (
        mkdir "!SMS_HOME!\%%d" >nul 2>&1
        call :log_debug "创建目录: !SMS_HOME!\%%d"
    )
)
goto :eof

REM 启动服务器
:start_server
call :log_info "启动SMS流媒体服务器..."

REM 切换到SMS_HOME目录
cd /d "!SMS_HOME!"

REM 构建启动命令
set "START_CMD=!JAVA_CMD! !JVM_OPTS! -jar boot.jar start"

call :log_debug "启动命令: !START_CMD!"

REM 启动服务器
if "%DAEMON%"=="true" (
    REM 后台运行
    start "SMS Server" /min !START_CMD!
    call :log_info "服务器已在后台启动"
) else (
    REM 前台运行
    !START_CMD!
)
goto :eof

REM 停止服务器
:stop_server
call :log_info "停止SMS流媒体服务器..."

REM 尝试通过Java进程名停止
taskkill /f /im java.exe /fi "WINDOWTITLE eq SMS*" >nul 2>&1
if not errorlevel 1 (
    call :log_info "服务器已停止"
) else (
    call :log_warn "未找到运行中的SMS服务器进程"
)
goto :eof

REM 显示服务器状态
:show_status
tasklist /fi "IMAGENAME eq java.exe" /fi "WINDOWTITLE eq SMS*" >nul 2>&1
if not errorlevel 1 (
    call :log_info "SMS服务器正在运行"
) else (
    call :log_info "SMS服务器未运行"
)
goto :eof

REM 显示帮助信息
:show_help
echo SMS 流媒体服务器启动脚本 (Windows版本)
echo.
echo 用法: %~nx0 [选项] [命令]
echo.
echo 命令:
echo   start    启动服务器
echo   stop     停止服务器
echo   restart  重启服务器
echo   status   显示服务器状态
echo   help     显示此帮助信息
echo.
echo 选项:
echo   /d       后台运行
echo   /h SIZE  设置堆内存大小 (例如: 1g, 512m)
echo   /j       启用JMX监控
echo   /p PORT  JMX端口 (默认: 9999)
echo   /debug   启用调试模式
echo   /?       显示此帮助信息
echo.
echo 环境变量:
echo   SMS_HOME         SMS安装目录
echo   JAVA_HOME        Java安装目录
echo   HEAP_SIZE        堆内存大小
echo   CUSTOM_JVM_OPTS  自定义JVM参数
echo.
echo 示例:
echo   %~nx0 start                    启动服务器
echo   %~nx0 /d start                 后台启动服务器
echo   %~nx0 /h 2g start              使用2GB堆内存启动
echo   %~nx0 /j /p 9999 start         启用JMX监控
goto :eof

REM 主函数
:main
REM 解析命令行参数
:parse_args
if "%~1"=="" goto :args_done

if /i "%~1"=="/d" (
    set "DAEMON=true"
    shift
    goto :parse_args
)
if /i "%~1"=="/h" (
    set "HEAP_SIZE=%~2"
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="/j" (
    set "ENABLE_JMX=true"
    shift
    goto :parse_args
)
if /i "%~1"=="/p" (
    set "JMX_PORT=%~2"
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="/debug" (
    set "DEBUG=true"
    shift
    goto :parse_args
)
if /i "%~1"=="/?" (
    call :show_help
    exit /b 0
)
if /i "%~1"=="start" (
    set "COMMAND=start"
    shift
    goto :parse_args
)
if /i "%~1"=="stop" (
    set "COMMAND=stop"
    shift
    goto :parse_args
)
if /i "%~1"=="restart" (
    set "COMMAND=restart"
    shift
    goto :parse_args
)
if /i "%~1"=="status" (
    set "COMMAND=status"
    shift
    goto :parse_args
)
if /i "%~1"=="help" (
    call :show_help
    exit /b 0
)

call :log_error "未知参数: %~1"
call :show_help
exit /b 1

:args_done

REM 默认命令
if "%COMMAND%"=="" set "COMMAND=start"

REM 设置默认值
if "%DAEMON%"=="" set "DAEMON=false"
if "%DEBUG%"=="" set "DEBUG=false"
if "%ENABLE_JMX%"=="" set "ENABLE_JMX=false"

REM 执行命令
if /i "%COMMAND%"=="start" (
    call :check_admin
    call :setup_sms_home
    call :check_java
    call :check_system_resources
    call :setup_jvm_options
    call :check_ports
    call :create_directories
    call :start_server
) else if /i "%COMMAND%"=="stop" (
    call :setup_sms_home
    call :stop_server
) else if /i "%COMMAND%"=="restart" (
    call :setup_sms_home
    call :stop_server
    timeout /t 2 /nobreak >nul
    call :check_admin
    call :check_java
    call :check_system_resources
    call :setup_jvm_options
    call :check_ports
    call :create_directories
    call :start_server
) else if /i "%COMMAND%"=="status" (
    call :setup_sms_home
    call :show_status
) else (
    call :log_error "未知命令: %COMMAND%"
    call :show_help
    exit /b 1
)

goto :eof

REM 执行主函数
call :main %*