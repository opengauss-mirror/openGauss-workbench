#!/bin/bash

APP_NAME="prometheus"
PID_FILE="prometheus.pid"
PORT="${port}"
STORAGEDAYS="${storageDays}"

start() {
  ./prometheus --config.file=prometheus.yml  --web.enable-lifecycle --web.listen-address=:$PORT --storage.tsdb.retention.time=$STORAGEDAYS > prometheus.log 2>&1 &
  count=0
  while [ $count -lt 100 ]; do
    prometheus_pid=$(ps aux | grep "config.file=prometheus.yml" | grep "web.listen-address=:$PORT" | grep "storage.tsdb.retention.time=$STORAGEDAYS" | grep -v grep | awk '{print $2}')
    if [ -n "$prometheus_pid" ]; then
      echo $prometheus_pid > $PID_FILE
      echo "Prometheus started. pid is $prometheus_pid"
      break
    fi
    sleep 0.1
    count=$((count+1))
  done
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