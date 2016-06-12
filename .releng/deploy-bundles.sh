#!/bin/sh

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')

cp "$ROOT/kernel/target/dependency/redist-"* "$1"
cp "$ROOT/kernel/target/dependency/xowl-"* "$1"
cp "$ROOT/kernel/target/dependency/shiro-"* "$1"
cp "$ROOT/kernel/target/dependency/slf4j-"* "$1"
cp "$ROOT/services/lts/target/dependency/xowl-"* "$1"
cp "$ROOT/kernel/target/xowl-kernel-$VERSION.jar" "$1"
cp "$ROOT/services/connection/target/xowl-service-connection-$VERSION.jar" "$1"
cp "$ROOT/services/consistency/target/xowl-service-consistency-$VERSION.jar" "$1"
cp "$ROOT/services/evaluation/target/xowl-service-evaluation-$VERSION.jar" "$1"
cp "$ROOT/services/executor/target/xowl-service-executor-$VERSION.jar" "$1"
cp "$ROOT/services/httpapi/target/xowl-service-httpapi-$VERSION.jar" "$1"
cp "$ROOT/services/impact/target/xowl-service-impact-$VERSION.jar" "$1"
cp "$ROOT/services/importation/target/xowl-service-importation-$VERSION.jar" "$1"
cp "$ROOT/services/lts/target/xowl-service-lts-$VERSION.jar" "$1"
cp "$ROOT/services/statistics/target/xowl-service-statistics-$VERSION.jar" "$1"
cp "$ROOT/services/webapp/target/xowl-service-webapp-$VERSION.jar" "$1"
cp "$ROOT/connectors/csv/target/xowl-connector-csv-$VERSION.jar" "$1"