#!/bin/bash

APP_NAME="filebeat"
PID_FILE="filebeat.pid"


start() {
  ./filebeat -e -c filebeat.yml > filebeat.log 2>&1 &
  echo $! > $PID_FILE
  echo "Filebeat started."
}

stop() {
  if [ -e $PID_FILE ]; then
    PID=$(cat $PID_FILE)
    kill $PID
    rm $PID_FILE
    echo "Filebeat stopped."
  else
    echo "Filebeat is not running."
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
      echo "Filebeat is running with PID: $PID"
    else
      echo "Stale PID file. Filebeat may not be running."
      rm $PID_FILE
    fi
  else
    echo "Filebeat is not running."
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