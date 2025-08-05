#!/bin/bash

APP_NAME="datakit"
JAR_PATTERN="openGauss-datakit-*.jar"
PID_FILE="datakit.pid"
LOG_DIR="logs"
LOG_FILE="${LOG_DIR}/visualtool-main.out"

mkdir -p "${LOG_DIR}"

get_jar_path() {
    local jar_file=$(ls -t ${JAR_PATTERN} 2>/dev/null | head -n 1)
    if [ -z "${jar_file}" ]; then
        echo "error : No matching JAR file found."
        exit 1
    fi

    if [ -f "${jar_file}" ]; then
        echo $(realpath "${jar_file}")
    else
        echo "error : JAR file not exist : ${jar_file}"
        exit 1
    fi
}

refresh_pid() {
    JAR_FILE=$(get_jar_path)
    PID=$(ps aux | grep java | grep -F -- "${JAR_FILE}" | grep -v grep | awk '{print $2}' | head -n 1)
    if [ -n "${PID}" ]; then
        echo "${PID}" > "${PID_FILE}"
    else
        rm -f "${PID_FILE}"
    fi
}

start_up() {
    JAR_FILE=$(get_jar_path)

    if [ -z "${DATA_KIT_AES_KEY}" ]; then
        echo "error : DATA_KIT_AES_KEY environment variable is not set."
        echo "        Please set it with --aes-key option"
        exit 1
    fi
    DEFAULT_JAVA_OPTS="-Xms2048m -Xmx4096m"
    JAVA_OPTS=${JAVA_OPTS:-$DEFAULT_JAVA_OPTS}
    echo "Starting ${APP_NAME} with DATA_KIT_AES_KEY..."
    export DATA_KIT_AES_KEY=${DATA_KIT_AES_KEY}
    nohup java ${JAVA_OPTS} -jar "${JAR_FILE}" --spring.profiles.active=temp > "${LOG_FILE}" 2>&1 &

    echo $! > "${PID_FILE}"
    echo "${APP_NAME} started. PID: $(cat ${PID_FILE})"
}

start() {
    refresh_pid

    if [ -s "${PID_FILE}" ]; then
        PID=$(cat "${PID_FILE}")
        if ps -p "${PID}" > /dev/null; then
            echo "Datakit is already running with PID : ${PID}"
            exit 1
        else
            echo "Stale PID file. Cleaning up."
            rm -f "${PID_FILE}"
        fi
    fi

    start_up
}

stop() {
    refresh_pid

    if [ -s "${PID_FILE}" ]; then
        PID=$(cat "${PID_FILE}")
        kill -9 "${PID}" >/dev/null 2>&1
        rm -f "${PID_FILE}"
        echo "${APP_NAME} stopped."
    else
        echo "${APP_NAME} is not running."
    fi
}

restart() {
    stop
    sleep 2
    start_up
}

status() {
    refresh_pid

    if [ -s "${PID_FILE}" ]; then
        PID=$(cat "${PID_FILE}")
        if ps -p "${PID}" > /dev/null; then
            echo "status : ${APP_NAME} is running with PID: ${PID}"
            echo "JAR PATH : $(get_jar_path)"
        else
            echo "warn : Stale PID file, ${APP_NAME} may not be running."
            rm -f "${PID_FILE}"
        fi
    else
        echo "status : ${APP_NAME} is not running."
    fi
}

show_help() {
    echo "Usage: $0 {start|stop|restart|status} [--aes-key <key>]"
    echo "Commands:"
    echo "  start              Start datakit service"
    echo "  stop               Stop datakit service"
    echo "  restart            Restart datakit service"
    echo "  status             Show service status"
    echo ""
    echo "Options:"
    echo "  --aes-key <key>    Set DATA_KIT_AES_KEY for this session"
    echo ""
    echo "Environment:"
    echo "  If DATA_KIT_AES_KEY is already set in environment,"
    echo "  --aes-key option will be ignored"
}

COMMAND="$1"
shift

while [[ $# -gt 0 ]]; do
    case "$1" in
        --aes-key)
            if [[ -z "$2" || "$2" == --* ]]; then
                echo "Error: --aes-key requires a value"
                show_help
                exit 1
            fi

            if [ -z "${DATA_KIT_AES_KEY}" ]; then
                export DATA_KIT_AES_KEY="$2"
                echo "Using command-line AES key"
            else
                echo "Warning: DATA_KIT_AES_KEY already set in environment. Ignoring --aes-key option"
            fi
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

case "${COMMAND}" in
    start)
        if [ -z "${DATA_KIT_AES_KEY}" ]; then
            echo "Error: AES key is required for 'start' command"
            echo "Set it via:"
            echo "  export DATA_KIT_AES_KEY='your_key'"
            echo "Or use: $0 start --aes-key your_key"
            exit 1
        fi

        if [ ${#DATA_KIT_AES_KEY} -lt 6 ]; then
            echo "Error: AES key must be at least 6 characters"
            exit 1
        fi

        echo "Starting service with AES key (length: ${#DATA_KIT_AES_KEY})"
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "Unknown command: ${COMMAND}"
        show_help
        exit 1
        ;;
esac

exit 0