#!/bin/sh

# Builds the Felix distribution
# This script assumes that the maven artifacts are built and that the dependencies are copied, as done in build.sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"


# Step 1 - Produce certificate store
# TODO: do this step at runtime

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
echo "org.apache.felix.https.keystore=felix/conf/keystore.jks" >> "$RELENG/config-https.properties"
echo "org.apache.felix.https.keystore.password=$PASSWORD" >> "$RELENG/config-https.properties"
echo "org.apache.felix.https.keystore.key.password=$PASSWORD" >> "$RELENG/config-https.properties"


# Step 2 - Build the base Felix platform

# Felix main distribution
wget -q -O "$RELENG/org.apache.felix.main.distribution-5.6.1.tar.gz" https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.main.distribution/5.6.1/org.apache.felix.main.distribution-5.6.1.tar.gz
tar -C "$RELENG" -xz -f "$RELENG/org.apache.felix.main.distribution-5.6.1.tar.gz"
rm "$RELENG/org.apache.felix.main.distribution-5.6.1.tar.gz"
mv "$RELENG/felix-framework-5.6.1" "$RELENG/felix"
# Required Felix bundles
wget -q -O "$RELENG/felix/bundle/org.apache.felix.configadmin-1.8.10.jar"      https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.configadmin/1.8.10/org.apache.felix.configadmin-1.8.10.jar
wget -q -O "$RELENG/felix/bundle/org.apache.felix.eventadmin-1.4.8.jar"       https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.eventadmin/1.4.8/org.apache.felix.eventadmin-1.4.8.jar
wget -q -O "$RELENG/felix/bundle/org.apache.felix.http.api-3.0.0.jar"         https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.api/3.0.0/org.apache.felix.http.api-3.0.0.jar
wget -q -O "$RELENG/felix/bundle/org.apache.felix.http.base-3.0.16.jar"        https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.base/3.0.16/org.apache.felix.http.base-3.0.16.jar
wget -q -O "$RELENG/felix/bundle/org.apache.felix.http.servlet-api-1.1.2.jar" https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.servlet-api/1.1.2/org.apache.felix.http.servlet-api-1.1.2.jar
wget -q -O "$RELENG/felix/bundle/org.apache.felix.http.jetty-3.4.0.jar"       https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.jetty/3.4.0/org.apache.felix.http.jetty-3.4.0.jar
# Deploy xOWL and its dependencies
sh "$RELENG/deploy-bundles.sh" "$RELENG/felix/bundle"
# copy the certificate store and HTTPS configuration
mv "$RELENG/keystore.jks" "$RELENG/felix/conf/"
cat "$RELENG/config-https.properties" >> "$RELENG/felix/conf/config.properties"
rm "$RELENG/config-https.properties"