#!/bin/sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')

rm -rf "$1/xowl-distribution"
tar -C "$1" -xf "$ROOT/distribution/target/xowl-distribution-$VERSION.tar.gz"
# first run to enforce the HTTP configuration
cd "$1/xowl-distribution"
java "-Dgosh.args=--noi" -jar "$1/xowl-distribution/felix/bin/felix.jar" -b "$1/xowl-distribution/felix/bundle" || true
