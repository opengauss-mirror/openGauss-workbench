#!/bin/bash

APP_NAME="datakit"
JAR_PATTERN="openGauss-datakit-*.jar"
PID_FILE="datakit.pid"

check_running() {
  if [ -e $PID_FILE ]; then
    PID=$(cat $PID_FILE)
    if ps -p $PID > /dev/null; then
      echo "Datakit is already running with PID: $PID"
      exit 1
    else
      echo "Stale PID file. Cleaning up."
      rm $PID_FILE
    fi
  fi
}

start() {
  check_running

  JAR_FILE=$(ls $JAR_PATTERN 2>/dev/null | tail -n 1)
  if [ -z "$JAR_FILE" ]; then
    echo "No matching JAR file found."
    exit 1
  fi
  nohup java -Xms2048m -Xmx4096m -jar $JAR_FILE --spring.profiles.active=temp > logs/visualtool-main.out 2>&1 &
  echo $! > $PID_FILE
  echo "Datakit started."
}

stop() {
  if [ -e $PID_FILE ]; then
    PID=$(cat $PID_FILE)
    kill $PID
    rm $PID_FILE
    echo "Datakit stopped."
  else
    echo "Datakit is not running."
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
      echo "Datakit is running with PID: $PID"
    else
      echo "Stale PID file. Datakit may not be running."
      rm $PID_FILE
    fi
  else
    echo "Datakit is not running."
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