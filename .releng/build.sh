#!/bin/sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

while [ $# -gt 0 ]; do
  case "$1" in
    --repository=*)
      REPOSITORY="${1#*=}"
      ;;
    *)
      printf "***************************\n"
      printf "* Error: Invalid argument.*\n"
      printf "***************************\n"
      exit 1
  esac
  shift
done

# Extract the product
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')
HASH=$(hg -R "$ROOT" --debug id -i)
echo "Building artifacts for version $VERSION ($HASH)"
tar -C "$RELENG/docker" -xf "$ROOT/distribution/target/xowl-distribution-$VERSION.tar.gz"

# Build the docker image
docker rmi "xowl/xowl-platform:$VERSION" || true
docker build --tag "xowl/xowl-platform:$VERSION" --rm "$RELENG/docker"

# Cleanup
rm -rf "$RELENG/docker/xowl-distribution"

# Publish
if [ ! -z "$REPOSITORY" ]; then
  docker tag  "xowl/xowl-platform:$VERSION" "$REPOSITORY/xowl/xowl-platform:$VERSION"
  docker tag  "$REPOSITORY/xowl/xowl-platform:$VERSION" "$REPOSITORY/xowl/xowl-platform:latest"
  docker push "$REPOSITORY/xowl/xowl-platform:$VERSION"
  docker push "$REPOSITORY/xowl/xowl-platform:latest"
  docker rmi  "$REPOSITORY/xowl/xowl-platform:$VERSION"
  docker rmi  "$REPOSITORY/xowl/xowl-platform:latest"
  docker rmi  "xowl/xowl-platform:$VERSION"
fi