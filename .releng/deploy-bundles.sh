#!/bin/sh

# Deploy the xOWL Platform bundles and their dependencies into a Felix distribution

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
doDeployDependency "$ROOT/kernel/target/dependency"                     "redist"            "org.xowl.hime"     "$1"
doDeployDependency "$ROOT/kernel/target/dependency"                     "xowl-commons"      "org.xowl.infra"    "$1"
doDeployDependency "$ROOT/kernel/target/dependency"                     "xowl-lang"         "org.xowl.infra"    "$1"
doDeployDependency "$ROOT/kernel/target/dependency"                     "xowl-store"        "org.xowl.infra"    "$1"
doDeployDependency "$ROOT/kernel/target/dependency"                     "xowl-server-api"   "org.xowl.infra"    "$1"
doDeployDependency "$ROOT/services/lts/target/dependency"               "xowl-engine"       "org.xowl.infra"    "$1"
doDeployDependency "$ROOT/services/lts/target/dependency"               "xowl-server"       "org.xowl.infra"    "$1"
doDeployDependency "$ROOT/services/security-shiro/target/dependency"    "shiro-core"        "org.apache.shiro"  "$1"
doDeployDependency "$ROOT/services/security-shiro/target/dependency"    "slf4j-api"         "org.slf4j"         "$1"
doDeployDependency "$ROOT/services/security-shiro/target/dependency"    "slf4j-simple"      "org.slf4j"         "$1"

# Deploy the platform bundles
doDeployBundle "$ROOT/kernel/target"                        "xowl-kernel-$VERSION.jar"                      "$1"
doDeployBundle "$ROOT/services/connection/target"           "xowl-service-connection-$VERSION.jar"          "$1"
doDeployBundle "$ROOT/services/consistency/target"          "xowl-service-consistency-$VERSION.jar"         "$1"
doDeployBundle "$ROOT/services/evaluation/target"           "xowl-service-evaluation-$VERSION.jar"          "$1"
doDeployBundle "$ROOT/services/httpapi/target"              "xowl-service-httpapi-$VERSION.jar"             "$1"
doDeployBundle "$ROOT/services/impact/target"               "xowl-service-impact-$VERSION.jar"              "$1"
doDeployBundle "$ROOT/services/importation/target"          "xowl-service-importation-$VERSION.jar"         "$1"
doDeployBundle "$ROOT/services/lts/target"                  "xowl-service-lts-$VERSION.jar"                 "$1"
doDeployBundle "$ROOT/services/security-internal/target"    "xowl-service-security-internal-$VERSION.jar"   "$1"
doDeployBundle "$ROOT/services/security-shiro/target"       "xowl-service-security-shiro-$VERSION.jar"      "$1"
doDeployBundle "$ROOT/services/webapp/target"               "xowl-service-webapp-$VERSION.jar"              "$1"
doDeployBundle "$ROOT/connectors/csv/target"                "xowl-connector-csv-$VERSION.jar"               "$1"
doDeployBundle "$ROOT/connectors/doors9/target"             "xowl-connector-doors9-$VERSION.jar"            "$1"