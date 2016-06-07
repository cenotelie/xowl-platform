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
rm -f "$TARGET/upload"
echo -n "federation12345" > "$TARGET/upload"
curl --insecure -u admin:admin -X PUT -T "$TARGET/upload" "$URI_LTS/user/federation" 2>/dev/null >/dev/null
rm -f "$TARGET/upload"
curl --insecure -u admin:admin -X PUT "$URI_LTS/db/federation_live" 2>/dev/null >/dev/null
curl --insecure -u admin:admin -X PUT "$URI_LTS/db/federation_long_term" 2>/dev/null >/dev/null
curl --insecure -u admin:admin -X PUT "$URI_LTS/db/federation_services" 2>/dev/null >/dev/null
curl --insecure -u admin:admin -X POST -H "Content-Type: application/x-xowl-xsp" "$URI_LTS/db/federation_live/privileges?action=grant&user=federation&access=ADMIN"  2>/dev/null >/dev/null
curl --insecure -u admin:admin -X POST -H "Content-Type: application/x-xowl-xsp" "$URI_LTS/db/federation_long_term/privileges?action=grant&user=federation&access=ADMIN"  2>/dev/null >/dev/null
curl --insecure -u admin:admin -X POST -H "Content-Type: application/x-xowl-xsp" "$URI_LTS/db/federation_services/privileges?action=grant&user=federation&access=ADMIN" 2>/dev/null >/dev/null
echo "OK!"

echo ""
echo ""
echo " 4. Launching xOWL Federation Platform"
docker run -d -i -p 8443:8443/tcp --name xowl-core -v "$TARGET/config":/config --link xowl-lts:xowl-lts "xowl/xowl-platform:$VERSION_FED"