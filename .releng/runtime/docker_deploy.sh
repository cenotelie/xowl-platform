#!/bin/sh

VERSION_LTS="2.0.0-SNAPSHOT"
VERSION_FED="0.2.4-SNAPSHOT"
URI_LTS="https://localhost:3443/api"

SCRIPT="$(readlink -f "$0")"
TARGET="$(dirname "$SCRIPT")"

echo " 1. Pulling Docker images"
docker pull "xowl/xowl-server:$VERSION_LTS"
docker pull "xowl/xowl-platform:$VERSION_FED"

echo ""
echo ""
echo " 2. Launching xOWL Triple Store"
rm -rf "$TARGET/db"
mkdir "$TARGET/db"
docker run -d -p 3443:3443/tcp --name xowl-lts -v "$TARGET/db":/xowl-data "xowl/xowl-server:$VERSION_LTS"
sleep 3
echo "OK!"

echo ""
echo ""
echo " 3. Setup xOWL Triple Store"
sh "$TARGET/db_init.sh"
echo "OK!"

echo ""
echo ""
echo " 4. Launching xOWL Federation Platform"
docker run -d -i -p 8443:8443/tcp --name xowl-core -v "$TARGET/config":/config --link xowl-lts:xowl-lts "xowl/xowl-platform:$VERSION_FED"