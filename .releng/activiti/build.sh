#!/bin/sh

SCRIPT="$(readlink -f "$0")"
DIR="$(dirname "$SCRIPT")"

# Build
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$DIR/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')
echo "Building artifacts for version $VERSION"
mvn install -f "$DIR/pom.xml"

# Build the docker image
docker build -t "xowl/xowl-activiti:$VERSION" "$DIR"