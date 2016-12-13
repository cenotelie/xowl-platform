#!/bin/sh

# Deploy the xOWL Platform bundles and their dependencies into a distribution
# This script expects parameters:
#  1 - The path to the distribution root

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -f "$ROOT/pom.xml" -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')


doDeployDependency () {
    FILE=$(ls $1/$2-*.jar | grep "$2-[0-9].*\(-SNAPSHOT\)\{0,1\}\.jar")
    FILE=$(basename "$FILE")
    cp "$1/$FILE" "$4/$3.$FILE"
}

doDeployBundle () {
    cp "$1/$2" "$3/org.xowl.platform.$2"
}


# Deploy the dependencies
doDeployDependency "$ROOT/kernel/target/dependency"                     "redist"            "org.xowl.hime"     "$1/felix/bundle"
doDeployDependency "$ROOT/kernel/target/dependency"                     "xowl-commons"      "org.xowl.infra"    "$1/felix/bundle"
doDeployDependency "$ROOT/kernel/target/dependency"                     "xowl-lang"         "org.xowl.infra"    "$1/felix/bundle"
doDeployDependency "$ROOT/kernel/target/dependency"                     "xowl-store"        "org.xowl.infra"    "$1/felix/bundle"
doDeployDependency "$ROOT/kernel/target/dependency"                     "xowl-server-api"   "org.xowl.infra"    "$1/felix/bundle"
doDeployDependency "$ROOT/services/storage/target/dependency"           "xowl-engine"       "org.xowl.infra"    "$1/felix/bundle"
doDeployDependency "$ROOT/services/storage/target/dependency"           "xowl-server"       "org.xowl.infra"    "$1/felix/bundle"
doDeployDependency "$ROOT/services/security-shiro/target/dependency"    "shiro-core"        "org.apache.shiro"  "$1/felix/bundle"
doDeployDependency "$ROOT/services/security-shiro/target/dependency"    "slf4j-api"         "org.slf4j"         "$1/felix/bundle"
doDeployDependency "$ROOT/services/security-shiro/target/dependency"    "slf4j-simple"      "org.slf4j"         "$1/felix/bundle"

# Deploy the platform bundles
doDeployBundle "$ROOT/kernel/target"                        "xowl-kernel-$VERSION.jar"                      "$1/felix/bundle"
doDeployBundle "$ROOT/services/connection/target"           "xowl-service-connection-$VERSION.jar"          "$1/felix/bundle"
doDeployBundle "$ROOT/services/consistency/target"          "xowl-service-consistency-$VERSION.jar"         "$1/felix/bundle"
doDeployBundle "$ROOT/services/evaluation/target"           "xowl-service-evaluation-$VERSION.jar"          "$1/felix/bundle"
doDeployBundle "$ROOT/services/httpapi/target"              "xowl-service-httpapi-$VERSION.jar"             "$1/felix/bundle"
doDeployBundle "$ROOT/services/impact/target"               "xowl-service-impact-$VERSION.jar"              "$1/felix/bundle"
doDeployBundle "$ROOT/services/importation/target"          "xowl-service-importation-$VERSION.jar"         "$1/felix/bundle"
doDeployBundle "$ROOT/services/storage/target"              "xowl-service-storage-$VERSION.jar"             "$1/felix/bundle"
doDeployBundle "$ROOT/services/security-internal/target"    "xowl-service-security-internal-$VERSION.jar"   "$1/felix/bundle"
doDeployBundle "$ROOT/services/security-shiro/target"       "xowl-service-security-shiro-$VERSION.jar"      "$1/felix/bundle"
doDeployBundle "$ROOT/services/webapp/target"               "xowl-service-webapp-$VERSION.jar"              "$1/felix/bundle"
doDeployBundle "$ROOT/connectors/csv/target"                "xowl-connector-csv-$VERSION.jar"               "$1/felix/bundle"
doDeployBundle "$ROOT/connectors/semanticweb/target"        "xowl-connector-semanticweb-$VERSION.jar"       "$1/felix/bundle"