#!/bin/sh

# Deploy the configuration into a distribution
# This script expects parameters:
#  1 - The path to the distribution root

SCRIPT="$(readlink -f "$0")"
RELENG="$(dirname "$SCRIPT")"
ROOT="$(dirname "$RELENG")"

rm -rf "$1/config/*"
cp "$RELENG/distribution/config/*" "$1/config"