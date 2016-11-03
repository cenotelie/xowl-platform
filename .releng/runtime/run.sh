#!/bin/sh

SCRIPT="$(readlink -f "$0")"
ROOT="$(dirname "$SCRIPT")"

java "-Dxowl.conf.dir=$ROOT/config" "-Dxowl.root=$ROOT" -jar "$ROOT/felix/bin/felix.jar" -b "$ROOT/felix/bundle"