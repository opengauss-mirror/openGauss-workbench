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
  nohup java -Xms2048m -Xmx4096m -jar "${JAR_FILE}" --spring.profiles.active=temp > "${LOG_FILE}" 2>&1 &
  echo $! > "${PID_FILE}"
  echo "${APP_NAME} started."
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
    kill "${PID}" >/dev/null 2>&1
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
          echo "JAR PAHT : $(get_jar_path)"
      else
          echo "warn : Stale PID file, ${APP_NAME} may not be running."
          rm -f "${PID_FILE}"
      fi
  else
      echo "status : ${APP_NAME} is not running."
  fi
}

case "$1" in
  start)
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
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac

exit 0