#!/bin/bash

set -m

APP_NAME="agent"
PID_FILE="agent.pid"


start() {
  $JAVA_VERSION -jar $JAR_NAME --diagnosis_host=$CALLBACK_URL  --agent_port=$PORT > agent.log 2>&1 &
  echo $! > $PID_FILE
  echo "Agent started."
}

stop() {
  if [ -e $PID_FILE ]; then
    PID=$(cat $PID_FILE)
    kill $PID
    rm $PID_FILE
    echo "Agent stopped."
  else
    echo "Agent is not running."
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
      echo "Agent is running with PID: $PID"
    else
      echo "Stale PID file. Agent may not be running."
      rm $PID_FILE
    fi
  else
    echo "Agent is not running."
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