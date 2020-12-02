#!/bin/bash
mockserver_home=$(cd `dirname $0`; pwd)
mockserver_port=6068
mockserver_lib="${mockserver_home}/lib/mockserver-netty-5.8.1-jar-with-dependencies.jar"
mockserver_expect_json="${mockserver_home}/conf/mock-server.json"
mockserver_log_file="${mockserver_home}/conf/logging.properties"

# --------------
# 启动mock-server
# --------------
function start() {
        # 获取配置文件
        echo -e "start mock-server..."
        nohup java -Djava.util.logging.config.file="${mockserver_log_file}" \
                -Dmockserver.initializationJsonPath="${mockserver_expect_json}" \
                -jar ${mockserver_lib} \
                -serverPort ${mockserver_port} \
                > /dev/null 2>&1 &
}

# --------------
# 停止mock-server
# --------------
function stop() {
        ps_pid=`ps -ef | grep ${mockserver_home} | grep -v grep | awk '{print $2}'`
        netstat_pid=`netstat -antp | grep "${mockserver_port} " | grep "LISTEN" | awk '{print $NF}' | awk -F"/" '{print $1}'`
        if [ ${ps_pid} == ${netstat_pid} ]
        then
                echo "stop mock-server...: ${ps_pid}"
                kill -9 ${ps_pid}
        else
                echo "${ps_pid} does not != ${netstat_pid}, \033[31m不知道需要杀死的进程对不对，请手动停止\033[0m"
        fi
}

# --------------
# 检查进程状态
# --------------
function status() {
        ps_pid=`ps -ef | grep ${mockserver_home} | grep -v grep | awk '{print $2}'`
        netstat_pid=`netstat -antp | grep "${mockserver_port} " | grep "LISTEN" | awk '{print $NF}' | awk -F"/" '{print $1}'`
        if [ ${ps_pid} == ${netstat_pid} ]
        then
                echo -e "status: \033[32mOK\033[0m pid ${ps_pid}"
        else
                echo "\033[31m请手动查询进程是否存在:ps -ef | grep mockserver_lib\033[0m"
        fi
}

# 帮助
function help() {
        echo -e "sh mockserver.sh \033[32m(start|stop|restart|status)\033[0m"
}

case $1 in
        start)
                start
                ;;
        stop)
                stop
                ;;
        status)
                status
                ;;
        restart)
                stop
                start
                ;;
        help)
                help
                ;;
        *)
                help
                ;;
esac
