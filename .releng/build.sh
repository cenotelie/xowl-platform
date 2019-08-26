#!/bin/sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

# Extract the product
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')
HASH=$(git rev-parse HEAD)
echo "Building artifacts for version $VERSION ($HASH)"
tar -C "$RELENG/docker" -xf "$ROOT/distribution/target/xowl-distribution-$VERSION.tar.gz"

# Build the docker image
docker rmi "xowl/xowl-platform:$VERSION" || true
docker build --tag "xowl/xowl-platform:$VERSION" --rm --label version="$VERSION" --label changeset="$HASH" "$RELENG/docker"

# Cleanup
rm -rf "$RELENG/docker/xowl-distribution"
