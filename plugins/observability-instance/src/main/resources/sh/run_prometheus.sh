#!/bin/bash

APP_NAME="prometheus"
PID_FILE="prometheus.pid"
PORT="${port}"
STORAGEDAYS="${storageDays}"

start() {
  nohup ./prometheus --config.file=prometheus.yml  --web.enable-lifecycle --web.listen-address=:$PORT --storage.tsdb.retention.time=$STORAGEDAYS > prometheus.log 2>&1 &
  echo $! > $PID_FILE
  echo "Prometheus started."
}

stop() {
  if [ -e $PID_FILE ]; then
    PID=$(cat $PID_FILE)
    kill $PID
    rm $PID_FILE
    echo "Prometheus stopped."
  else
    echo "Prometheus is not running."
  fi
}

restart() {
  stop
  start
}

status() {
  if [ -e $PID_FILE ]; then
    PID=$(cat $PID_FILE)
    if ps -p $PID > /dev/null; then
      echo "Prometheus is running with PID: $PID"
    else
      echo "Stale PID file. Prometheus may not be running."
      rm $PID_FILE
    fi
  else
    echo "Prometheus is not running."
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