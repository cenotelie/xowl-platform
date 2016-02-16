#!/bin/sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

# Build
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')
echo "Building artifacts for version $VERSION"
mvn install -f "$ROOT/pom.xml" -DskipTests -Dgpg.skip=true
mvn dependency:copy-dependencies -f "$ROOT/kernel/pom.xml"

# Extract the bundles
cp "$ROOT/kernel/target/dependency/redist-"* "$RELENG/docker/"
cp "$ROOT/kernel/target/dependency/xowl-"* "$RELENG/docker/"
cp "$ROOT/kernel/target/dependency/shiro-"* "$RELENG/docker/"
cp "$ROOT/kernel/target/dependency/slf4j-"* "$RELENG/docker/"
cp "$ROOT/kernel/target/xowl-kernel-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/connection/target/xowl-service-connection-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/consistency/target/xowl-service-consistency-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/evaluation/target/xowl-service-evaluation-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/executor/target/xowl-service-executor-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/httpapi/target/xowl-service-httpapi-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/lts/target/xowl-service-lts-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/statistics/target/xowl-service-statistics-$VERSION.jar" "$RELENG/docker/"
cp "$ROOT/services/webapp/target/xowl-service-webapp-$VERSION.jar" "$RELENG/docker/"

# Build the keystore for the certificate
CERT_CN="platform.xowl.org"
PASSWORD="$(< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c${1:-32};echo;)"

rm -f "$RELENG/pwd.txt"
rm -f "$RELENG/docker/config-https.properties"
rm -f "$RELENG/docker/keystore.jks"
echo "$PASSWORD" >> "$RELENG/pwd.txt"
echo "$PASSWORD" >> "$RELENG/pwd.txt"
echo "" >> "$RELENG/pwd.txt"

keytool -genkeypair -alias "$CERT_CN" -keyalg RSA -keysize 2048 -dname "CN=$CERT_CN, O=xowl.org" -validity 3650 -storetype JKS -keystore "$RELENG/docker/keystore.jks" < "$RELENG/pwd.txt"
rm -f "$RELENG/pwd.txt"
echo "org.osgi.service.http.port.secure=8443" >> "$RELENG/docker/config-https.properties"
echo "org.apache.felix.https.enable=true" >> "$RELENG/docker/config-https.properties"
echo "org.apache.felix.https.keystore=felix-framework-5.4.0/conf/keystore.jks" >> "$RELENG/docker/config-https.properties"
echo "org.apache.felix.https.keystore.password=$PASSWORD" >> "$RELENG/docker/config-https.properties"
echo "org.apache.felix.https.keystore.key.password=$PASSWORD" >> "$RELENG/docker/config-https.properties"

# Build the docker image
docker build -t "xowl/xowl-platform:$VERSION" "$RELENG/docker"

# Cleanup
rm "$RELENG/docker/"*.jar
rm "$RELENG/docker/config-https.properties"
rm "$RELENG/docker/keystore.jks"