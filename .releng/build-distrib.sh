#!/bin/sh

# Builds the Felix distribution
# This script assumes that the maven artifacts are built and that the dependencies are copied, as done in build.sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
DISTRIB="$RELENG/distribution"

# Felix main distribution
wget -q -O "$DISTRIB/org.apache.felix.main.distribution-5.6.1.tar.gz" https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.main.distribution/5.6.1/org.apache.felix.main.distribution-5.6.1.tar.gz
tar -C "$DISTRIB" -xz -f "$DISTRIB/org.apache.felix.main.distribution-5.6.1.tar.gz"
rm "$DISTRIB/org.apache.felix.main.distribution-5.6.1.tar.gz"
mv "$DISTRIB/felix-framework-5.6.1" "$DISTRIB/felix"
rm -rf "$DISTRIB/felix/doc"
# Required Felix bundles
wget -q -O "$DISTRIB/felix/bundle/org.apache.felix.configadmin-1.8.10.jar"      https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.configadmin/1.8.10/org.apache.felix.configadmin-1.8.10.jar
wget -q -O "$DISTRIB/felix/bundle/org.apache.felix.eventadmin-1.4.8.jar"        https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.eventadmin/1.4.8/org.apache.felix.eventadmin-1.4.8.jar
wget -q -O "$DISTRIB/felix/bundle/org.apache.felix.http.api-3.0.0.jar"          https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.api/3.0.0/org.apache.felix.http.api-3.0.0.jar
wget -q -O "$DISTRIB/felix/bundle/org.apache.felix.http.base-3.0.16.jar"        https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.base/3.0.16/org.apache.felix.http.base-3.0.16.jar
wget -q -O "$DISTRIB/felix/bundle/org.apache.felix.http.servlet-api-1.1.2.jar"  https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.servlet-api/1.1.2/org.apache.felix.http.servlet-api-1.1.2.jar
wget -q -O "$DISTRIB/felix/bundle/org.apache.felix.http.jetty-3.4.0.jar"        https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.jetty/3.4.0/org.apache.felix.http.jetty-3.4.0.jar
# Deploy xOWL and its dependencies
sh "$RELENG/deploy-bundles.sh" "$DISTRIB"