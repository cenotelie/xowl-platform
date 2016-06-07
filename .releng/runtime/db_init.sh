#!/bin/sh

URI="https://localhost:3443/api"

SCRIPT="$(readlink -f "$0")"
TARGET="$(dirname "$SCRIPT")"

rm -f "$TARGET/upload"
echo -n "federation12345" > "$TARGET/upload"
curl --insecure -u admin:admin -X PUT -T "$TARGET/upload" "$URI/user/federation" 2>/dev/null >/dev/null
rm -f "$TARGET/upload"
curl --insecure -u admin:admin -X PUT "$URI/db/federation_live" 2>/dev/null >/dev/null
curl --insecure -u admin:admin -X PUT "$URI/db/federation_long_term" 2>/dev/null >/dev/null
curl --insecure -u admin:admin -X PUT "$URI/db/federation_services" 2>/dev/null >/dev/null
curl --insecure -u admin:admin -X POST -H "Content-Type: application/x-xowl-xsp" "$URI/db/federation_live/privileges?action=grant&user=federation&access=ADMIN"  2>/dev/null >/dev/null
curl --insecure -u admin:admin -X POST -H "Content-Type: application/x-xowl-xsp" "$URI/db/federation_long_term/privileges?action=grant&user=federation&access=ADMIN"  2>/dev/null >/dev/null
curl --insecure -u admin:admin -X POST -H "Content-Type: application/x-xowl-xsp" "$URI/db/federation_services/privileges?action=grant&user=federation&access=ADMIN" 2>/dev/null >/dev/null