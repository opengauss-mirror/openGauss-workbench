#!/bin/bash
###
# Copyright (c) 2023 Huawei Technologies Co., Ltd.
# openGauss-workbench is licensed under Mulan PSL v2.
# You can use this software according to the terms and conditions of the Mulan PSL v2.
# You may obtain a copy of Mulan PSL v2 at:
#          http://license.coscl.org.cn/MulanPSL2
# THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
# EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
# MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
# See the Mulan PSL v2 for more details.
###

APP_NAME="instance-exporter"
PID_FILE="instance-exporter.pid"
JAVA="${java}"
PORT="${port}"

start() {
  nohup $JAVA -jar instance-exporter.jar --server.port=$PORT >/dev/null 2>&1 &
  count=0
  while [ $count -lt 100 ]; do
    agent_pid=$(ps aux | grep "instance-exporter.jar" | grep "server.port=$PORT" | grep -v grep | awk '{print $2}')
    if [ -n "$agent_pid" ]; then
      echo $agent_pid > $PID_FILE
      echo "Agent started. pid is $agent_pid"
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