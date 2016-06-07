#!/bin/sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

# Build the sources
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')
echo "Building artifacts for version $VERSION"
mvn install -f "$ROOT/pom.xml" -DskipTests -Dgpg.skip=true
mvn dependency:copy-dependencies -f "$ROOT/kernel/pom.xml"



# Produce certificate store
CERT_CN="platform.xowl.org"
PASSWORD="$(< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c${1:-32};echo;)"

rm -f "$RELENG/pwd.txt"
rm -f "$RELENG/config-https.properties"
rm -f "$RELENG/keystore.jks"
echo "$PASSWORD" >> "$RELENG/pwd.txt"
echo "$PASSWORD" >> "$RELENG/pwd.txt"
echo "" >> "$RELENG/pwd.txt"

keytool -genkeypair -alias "$CERT_CN" -keyalg RSA -keysize 2048 -dname "CN=$CERT_CN, O=xowl.org" -validity 3650 -storetype JKS -keystore "$RELENG/keystore.jks" < "$RELENG/pwd.txt"
rm -f "$RELENG/pwd.txt"
echo "org.osgi.service.http.port.secure=8443" >> "$RELENG/config-https.properties"
echo "org.apache.felix.https.enable=true" >> "$RELENG/config-https.properties"
echo "org.apache.felix.https.keystore=felix-framework-5.4.0/conf/keystore.jks" >> "$RELENG/config-https.properties"
echo "org.apache.felix.https.keystore.password=$PASSWORD" >> "$RELENG/config-https.properties"
echo "org.apache.felix.https.keystore.key.password=$PASSWORD" >> "$RELENG/config-https.properties"



# Build the distribution
wget -q -O "$RELENG/org.apache.felix.main.distribution-5.4.0.tar.gz" https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.main.distribution/5.4.0/org.apache.felix.main.distribution-5.4.0.tar.gz
tar -C "$RELENG" -xz -f "$RELENG/org.apache.felix.main.distribution-5.4.0.tar.gz"
rm "$RELENG/org.apache.felix.main.distribution-5.4.0.tar.gz"
wget -q -O "$RELENG/felix-framework-5.4.0/bundle/org.apache.felix.configadmin-1.8.8.jar"      https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.configadmin/1.8.8/org.apache.felix.configadmin-1.8.8.jar
wget -q -O "$RELENG/felix-framework-5.4.0/bundle/org.apache.felix.eventadmin-1.4.6.jar"       https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.eventadmin/1.4.6/org.apache.felix.eventadmin-1.4.6.jar
wget -q -O "$RELENG/felix-framework-5.4.0/bundle/org.apache.felix.http.api-3.0.0.jar"         https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.api/3.0.0/org.apache.felix.http.api-3.0.0.jar
wget -q -O "$RELENG/felix-framework-5.4.0/bundle/org.apache.felix.http.base-3.0.4.jar"        https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.base/3.0.4/org.apache.felix.http.base-3.0.4.jar
wget -q -O "$RELENG/felix-framework-5.4.0/bundle/org.apache.felix.http.servlet-api-1.1.2.jar" https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.servlet-api/1.1.2/org.apache.felix.http.servlet-api-1.1.2.jar
wget -q -O "$RELENG/felix-framework-5.4.0/bundle/org.apache.felix.http.jetty-3.1.6.jar"       https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.jetty/3.1.6/org.apache.felix.http.jetty-3.1.6.jar
cp "$ROOT/kernel/target/dependency/redist-"* "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/kernel/target/dependency/xowl-"* "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/kernel/target/dependency/shiro-"* "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/kernel/target/dependency/slf4j-"* "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/kernel/target/xowl-kernel-$VERSION.jar" "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/services/connection/target/xowl-service-connection-$VERSION.jar" "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/services/consistency/target/xowl-service-consistency-$VERSION.jar" "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/services/evaluation/target/xowl-service-evaluation-$VERSION.jar" "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/services/executor/target/xowl-service-executor-$VERSION.jar" "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/services/httpapi/target/xowl-service-httpapi-$VERSION.jar" "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/services/impact/target/xowl-service-impact-$VERSION.jar" "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/services/lts/target/xowl-service-lts-$VERSION.jar" "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/services/statistics/target/xowl-service-statistics-$VERSION.jar" "$RELENG/felix-framework-5.4.0/bundle/"
cp "$ROOT/services/webapp/target/xowl-service-webapp-$VERSION.jar" "$RELENG/felix-framework-5.4.0/bundle/"

mv "$RELENG/keystore.jks" "$RELENG/felix-framework-5.4.0/conf/"
cat "$RELENG/config-https.properties" >> "$RELENG/felix-framework-5.4.0/conf/config.properties"
rm "$RELENG/config-https.properties"



# Package distribution
tar -czf "$RELENG/xowl-platform-$VERSION.tar.gz" -C "$ROOT" LICENSE.txt -C "$RELENG" "felix-framework-5.4.0/" -C "$RELENG/runtime" "run.sh" "config/"



# Build the docker image
mv "$RELENG/felix-framework-5.4.0" "$RELENG/docker/felix-framework-5.4.0"
docker build -t "xowl/xowl-platform:$VERSION" "$RELENG/docker"

# Cleanup
rm -rf "$RELENG/docker/felix-framework-5.4.0"