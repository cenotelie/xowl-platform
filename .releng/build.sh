#!/bin/sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

# Build the sources
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')
echo "Building artifacts for version $VERSION"
mvn clean install -f "$ROOT/pom.xml" -Dgpg.skip=true
mvn dependency:copy-dependencies -f "$ROOT/pom.xml"

# Build the distribution
sh "$RELENG/build-distrib.sh"

# Package distribution
tar -czf "$RELENG/xowl-platform-$VERSION.tar.gz" -C "$ROOT" LICENSE.txt -C "$RELENG" "felix/" -C "$RELENG/runtime" "run.sh" "config/"

# Build the docker image
mv "$RELENG/felix" "$RELENG/docker/felix"
cp -r "$RELENG/runtime/config" "$RELENG/docker/config"
docker build -t "xowl/xowl-platform:$VERSION" "$RELENG/docker"

# Cleanup
rm -rf "$RELENG/docker/felix"
rm -rf "$RELENG/docker/config"