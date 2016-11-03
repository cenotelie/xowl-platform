#!/bin/sh

SCRIPT="$(readlink -f "$0")"
ROOT="$(dirname "$SCRIPT")"

CODE=5
while [ "$CODE" -eq 5 ]
  do
    java "-Dxowl.conf.dir=$ROOT/config" "-Dxowl.root=$ROOT/felix" -jar "$ROOT/felix/bin/felix.jar" -b "$ROOT/felix/bundle"
    CODE=$?
    echo "Exit code is $CODE"
done