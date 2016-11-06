#!/bin/sh

# number of seconds to wait after launching the daemon before checking it has started correctly
STARTUP_WAIT=4

SCRIPT="$(readlink -f "$0")"
DISTRIB="$(dirname $SCRIPT)"

init () {
  PID=
  ISRUNNING=false

  if [ -f "xowl-platform.pid" ];
  then
    # pid file exists, is the server still running?
    PID=`cat "$DISTRIB/xowl-platform.pid"`
    PROCESS=`ps -p $PID`
    TARGET=sh
    if test "${PROCESS#*$TARGET}" != "$PROCESS"
    then
      ISRUNNING=true
    fi
  fi
}

start () {
  if [ "$ISRUNNING" = "true" ]; then
    echo "xOWL Platform is already running ..."
    exit 1
  else
    doStart
    if [ "$ISRUNNING" = "true" ]; then
      exit 0
    else
      exit 1
    fi
  fi
}

doStart () {
  echo "==== xOWL Platform Startup ====" >> log.txt
  sh "$DISTRIB/do-run.sh" &
  PID="$!"
  echo $PID > "$DISTRIB/xowl-platform.pid"
  sleep $STARTUP_WAIT
  PROCESS=`ps -p $PID`
  TARGET=sh
  if test "${PROCESS#*$TARGET}" != "$PROCESS"
  then
    ISRUNNING=true
    echo "xOWL Platform started ..."
  else
    ISRUNNING=false
    echo "xOWL Platform failed to start"
  fi
}

stop () {
  if [ "$ISRUNNING" = "true" ]; then
    doStop
    exit 0
  else
    echo "xOWL Platform is not running ..."
    exit 0
  fi
}

doStop () {
  echo "xOWL Platform stopping ..."
  PGID=`ps -o pgid= -p 6162 | tr -d ' '`
  kill -TERM -$PGID
  rm "$DISTRIB/xowl-platform.pid"
  echo "xOWL Platform stopped ..."
}

restart () {
  if [ "$ISRUNNING" = "true" ]; then
    doStop
  fi
  doStart
  if [ "$ISRUNNING" = "true" ]; then
    exit 0
  else
    exit 1
  fi
}

status () {
  if [ "$ISRUNNING" = "true" ]; then
    echo "xOWL Platform is running on PID $PID"
  else
    echo "xOWL Platform is not running"
  fi
  exit 0
}

## Main script
# set the current directory to the script's location
cd "$(dirname "$0")"
# initialize
init
# do we have exactly one argument?
if [ "$#" -ne 1 ]; then
  echo "admin.sh start|stop|restart|status"
  exit 1
fi
# OK, branch on the command
if [ "$1" = "start" ]; then
  start
elif [ "$1" = "stop" ]; then
  stop
elif [ "$1" = "restart" ]; then
  restart
elif [ "$1" = "status" ]; then
  status
elif [ "$1" = "help" ]; then
  echo "admin.sh start|stop|restart|status"
  exit 0
else
  echo "Unknown command"
  exit 1
fi
