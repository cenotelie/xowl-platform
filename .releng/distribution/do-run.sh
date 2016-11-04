#!/bin/sh

# Runs an instance of the xOWL platform
# This scripts looks for a special return value to indicate that the server requested a restart

SCRIPT="$(readlink -f "$0")"
DISTRIB="$(dirname "$SCRIPT")"

# custom path to java, if any
# if left empty, the script will use 'which' to locate java
JAVA=

if [ -z $JAVA ]
  then
    JAVA=`which java`
fi
if [ -z $JAVA ]
  then
    echo "Cannot find java"
    exit 1
fi

# while exit code is 5 (restart) relaunch the process
CODE=5
while [ "$CODE" -eq 5 ]
  do
    $JAVA "-Dxowl.root=$DISTRIB" -jar "$DISTRIB/felix/bin/felix.jar" -b "$DISTRIB/felix/bundle"
    CODE=$?
    echo "Exit code is $CODE"
done