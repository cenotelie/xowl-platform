#!/bin/sh

CODE=5
while [ "$CODE" -eq 5 ]
  do
    java "-Dxowl.conf.dir=/config" "-Dxowl.root=felix" -jar "felix/bin/felix.jar" -b "felix/bundle"
    CODE=$?
    echo "Exit code is $CODE"
done