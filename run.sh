#!/bin/bash

# Prevent execution as root user
if [ "$(id -u)" -eq 0 ]; then
    echo "Error: DataKit cannot be executed as root user"
    echo "Please run with a regular user: su <username> "
    exit 1
fi

# Fix the working directory to the script location so all relative paths
# (config, pid, logs, jar pattern) work regardless of the caller's cwd.
# Resolve to an absolute path first: 'cd .' fails (ENOTDIR) when the
# caller's working directory handle is stale (e.g. dir recreated elsewhere).
case "$0" in
    /*) SCRIPT_DIR=$(dirname -- "$0") ;;
    *)  SCRIPT_DIR=$(dirname -- "${PWD}/$0") ;;
esac
APP_HOME=$(cd -- "${SCRIPT_DIR}" 2>/dev/null && pwd -P) || APP_HOME=""
if [ -z "${APP_HOME}" ] || [ ! -d "${APP_HOME}" ]; then
    echo "Error: cannot determine DataKit home directory (script: ${0}, cwd: ${PWD})"
    echo "Hint: run this script with its absolute path, or re-login and retry."
    exit 1
fi
cd "${APP_HOME}" || exit 1

# Optional per-installation environment written by privileged install
# ('sudo sh install.sh'): DATAKIT_RUN_USER / DATAKIT_USER_HOME /
# DATAKIT_LOG_DIR / DATAKIT_PID_FILE
if [ -f "${APP_HOME}/config/run-env.sh" ]; then
    # shellcheck disable=SC1091
    . "${APP_HOME}/config/run-env.sh"
fi

# Optional dedicated account check: when DATAKIT_RUN_USER is set (e.g. 'datakit'),
# refuse to start under any other account. Keep empty for backward compatibility.
RUN_USER="${DATAKIT_RUN_USER:-}"
if [ -n "${RUN_USER}" ] && [ "$(id -un)" != "${RUN_USER}" ]; then
    echo "Error: DataKit must run as user '${RUN_USER}'"
    echo "Current user: $(id -un)"
    exit 1
fi

umask 027

APP_NAME="datakit"
JAR_PATTERN="openGauss-datakit-*.jar"
# PID file and startup log can be redirected outside a root-owned install dir
# (standard mode via config/run-env.sh written by 'sudo sh install.sh')
PID_FILE="${DATAKIT_PID_FILE:-datakit.pid}"
LOG_DIR="${DATAKIT_LOG_DIR:-logs}"
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

check_and_fix_log_permissions() {
    # operate on the caller's log dir via parameter; do NOT touch the global
    # LOG_DIR (it may point to the privileged data dir from config/run-env.sh)
    local target_dir="$1"
    local EXPECTED_FILE_PERM="640"
    local EXPECTED_DIR_PERM="750"

    echo "check log files and dir permissions..."

    if [ -d "$target_dir" ]; then
        local dir_perm=$(stat -c "%a" "$target_dir")
        if [ "$dir_perm" != "$EXPECTED_DIR_PERM" ]; then
            echo "  fixed log dir permissions: $dir_perm -> $EXPECTED_DIR_PERM"
            chmod "$EXPECTED_DIR_PERM" "$target_dir"
        fi
    fi

    find "$target_dir" -maxdepth 1 -name "*.log" -type f 2>/dev/null | while read -r log_file; do
        local file_perm=$(stat -c "%a" "$log_file")
        if [ "$file_perm" != "$EXPECTED_FILE_PERM" ]; then
            echo "  fixed log files permissions: $log_file ($file_perm -> $EXPECTED_FILE_PERM)"

            if lsof "$log_file" >/dev/null 2>&1; then
                echo "  warn: file is used ,not fixed permissions: $log_file"
            else
                chmod "$EXPECTED_FILE_PERM" "$log_file"
            fi
        fi
    done

    umask 027
}

start_up() {
    JAR_FILE=$(get_jar_path)

    if [ -z "${DATA_KIT_AES_KEY}" ]; then
        echo "error : DATA_KIT_AES_KEY environment variable is not set."
        echo "        Please set it with --aes-key option"
        exit 1
    fi

    check_and_fix_log_permissions "${LOG_DIR}"

    DEFAULT_JAVA_OPTS="-Xms2048m -Xmx4096m"
    JAVA_OPTS=${JAVA_OPTS:-$DEFAULT_JAVA_OPTS}
    echo "Starting ${APP_NAME} with DATA_KIT_AES_KEY..."
    export DATA_KIT_AES_KEY=${DATA_KIT_AES_KEY}
    # Privileged mode: redirect 'user.home' to the data root so the AES keystore
    # (~/.secure/datakit.jceks) is written to run-account-owned storage instead
    # of the account home dir, which may not exist (useradd -M)
    local user_home_opt=""
    if [ -n "${DATAKIT_USER_HOME}" ]; then
        user_home_opt="-Duser.home=${DATAKIT_USER_HOME}"
    fi

    # log4j2.xml (main + all plugins) resolves LOG_PATH from the system property
    # 'logging.file.path' and falls back to './logs' — which is unwritable for the
    # run account in privileged mode (root-owned install dir). Pass it explicitly.
    nohup java ${JAVA_OPTS} ${user_home_opt} -Dlogging.file.path="${LOG_DIR}" -Dlog4j.configurationFile=./config/log4j2.xml -Dfile.encoding=UTF-8 -jar "${JAR_FILE}" --spring.profiles.active=temp > "${LOG_FILE}" 2>&1 &

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
        # graceful shutdown first (SIGTERM triggers the Spring shutdown hook),
        # fall back to SIGKILL only if the process is still alive after 30s
        kill -15 "${PID}" >/dev/null 2>&1
        for _ in $(seq 1 30); do
            if ! ps -p "${PID}" > /dev/null 2>&1; then
                break
            fi
            sleep 1
        done
        if ps -p "${PID}" > /dev/null 2>&1; then
            echo "Process ${PID} still alive after 30s, sending SIGKILL..."
            kill -9 "${PID}" >/dev/null 2>&1
        fi
        rm -f "${PID_FILE}"
        echo "${APP_NAME} stopped."
    else
        echo "${APP_NAME} is not running."
    fi
}

restart() {
    # validate BEFORE stopping: a restart without the key must leave the
    # running service untouched instead of stopping it and then failing
    if [ -z "${DATA_KIT_AES_KEY}" ]; then
        echo "Error: AES key is required for 'restart' command"
        echo "Set it via:"
        echo "  export DATA_KIT_AES_KEY='your_key'"
        echo "Or use: $0 restart --aes-key your_key"
        exit 1
    fi
    if [ ${#DATA_KIT_AES_KEY} -lt 6 ]; then
        echo "Error: AES key must be at least 6 characters"
        exit 1
    fi
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
    echo "                     Warning: command-line arguments are visible in 'ps' output."
    echo "                     Prefer 'export DATA_KIT_AES_KEY=...' instead."
    echo ""
    echo "Environment:"
    echo "  If DATA_KIT_AES_KEY is already set in environment,"
    echo "  --aes-key option will be ignored"
    echo "  DATAKIT_RUN_USER   When set, DataKit refuses to start under any other user"
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