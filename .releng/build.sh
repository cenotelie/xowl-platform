#!/bin/sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

# Build
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')
echo "Building artifacts for version $VERSION"
mvn package -f "$ROOT/pom.xml" -DskipTests -Dgpg.skip=true
mvn dependency:copy-dependencies -f "$ROOT/kernel/pom.xml"

# Extract the bundles
cp "$ROOT/kernel/target/dependency/redist-"* "$RELENG/docker/"
cp "$ROOT/kernel/target/dependency/xowl-"* "$RELENG/docker/"
cp "$ROOT/kernel/target/xowl-kernel-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/consistency/target/xowl-service-consistency-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/domain/target/xowl-service-domain-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/executor/target/xowl-service-executor-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/httpapi/target/xowl-service-httpapi-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/lts/target/xowl-service-lts-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/statistics/target/xowl-service-statistics-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/webapp/target/xowl-service-webapp-$VERSION.jar" "$RELENG/docker/"

# Build the docker image
docker build -t "xowl/xowl-platform:$VERSION" "$RELENG/docker"

# Cleanup
rm "$RELENG/docker/"*.jar
