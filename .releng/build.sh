#!/bin/sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

# Build
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')
echo "Building artifacts for version $VERSION"
mvn package -f "$ROOT/pom.xml" -DskipTests -Dgpg.skip=true

# Extract the bundles
cp "$ROOT/dependencies/target/xowl-dependencies-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/kernel/target/xowl-kernel-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/service-config/target/xowl-service-config-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/service-domain/target/xowl-service-domain-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/service-lts/target/xowl-service-lts-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/service-server/target/xowl-service-server-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/service-workflow/target/xowl-service-workflow-$VERSION.jar" "$RELENG/docker/"

# Build the docker image
docker build -t xowl/xowl-platform:$VERSION "$RELENG/docker"

# Cleanup
rm "$RELENG/docker/xowl-dependencies-$VERSION.jar"
rm "$RELENG/docker/xowl-kernel-$VERSION.jar"
rm "$RELENG/docker/xowl-service-config-$VERSION.jar"
rm "$RELENG/docker/xowl-service-domain-$VERSION.jar"
rm "$RELENG/docker/xowl-service-lts-$VERSION.jar"
rm "$RELENG/docker/xowl-service-server-$VERSION.jar"
rm "$RELENG/docker/xowl-service-workflow-$VERSION.jar"
