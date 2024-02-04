#!/bin/bash

APP_NAME="datakit"
JAR_PATTERN="openGauss-datakit-*.jar"
PID_FILE="datakit.pid"

refresh_pid() {
  JAR_FILE=$(ls $JAR_PATTERN 2>/dev/null | tail -n 1)
  if [ -z "$JAR_FILE" ]; then
    echo "No matching JAR file found."
    exit 1
  fi
  SEVER_PATH=$(pwd)
  PID=$(ps -f --sort=start | grep "$JAR_FILE" | grep java | awk '{print $2}' | xargs -I{} pwdx {} | grep "$SEVER_PATH" | awk '{print $1}' | sed 's/.$//' | tail -n 1)
  if [ -n "$PID" ]; then
    echo $PID > $PID_FILE
  fi
}

start_up() {
  nohup java -Xms2048m -Xmx4096m -jar $JAR_FILE --spring.profiles.active=temp > logs/visualtool-main.out 2>&1 &
  echo $! > $PID_FILE
  echo "Datakit started."
}

start() {
  refresh_pid

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

  start_up
}

stop() {
  refresh_pid

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
  start_up
}

status() {
  refresh_pid

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