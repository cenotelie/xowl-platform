#!/bin/sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

# Build the sources
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')
HASH=$(hg -R "$ROOT" --debug id -i)
echo "Building artifacts for version $VERSION ($HASH)"
mvn clean install -f "$ROOT/pom.xml" -Dgpg.skip=true
mvn dependency:copy-dependencies -f "$ROOT/pom.xml"

# build version file
rm -f "$RELENG/xowl-platform.version"
touch "$RELENG/xowl-platform.version"
echo "version = $VERSION" >> "$RELENG/xowl-platform.version"
echo "changeset = $HASH" >> "$RELENG/xowl-platform.version"

# Build the distribution
sh "$RELENG/build-distrib.sh"

# Package distribution
rm -f "$RELENG/xowl-platform-$VERSION.tar.gz"
tar -czf "$RELENG/xowl-platform-$VERSION.tar.gz" -C "$ROOT" LICENSE.txt -C "$RELENG" "xowl-platform.version" -C "$RELENG/distribution" "felix/" "config/" "do-run.sh" "admin.sh" "install-daemon.sh" "uninstall-daemon.sh"

# Build the docker image
mv "$RELENG/xowl-platform.version" "$RELENG/docker/xowl-platform.version"
mv "$RELENG/distribution/felix" "$RELENG/docker/felix"
cp "$RELENG/distribution/do-run.sh" "$RELENG/docker/do-run.sh"
cp -r "$RELENG/distribution/config" "$RELENG/docker/config"
docker rmi "xowl/xowl-platform:$VERSION" || true
docker build -t "xowl/xowl-platform:$VERSION" "$RELENG/docker"

# Cleanup
rm -rf "$RELENG/docker/felix"
rm -rf "$RELENG/docker/config"
rm -f "$RELENG/docker/do-run.sh"
rm -f "$RELENG/docker/xowl-platform.version"