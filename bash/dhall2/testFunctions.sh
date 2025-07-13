#!/usr/bin/env bash
set -euo pipefail
source dhallFunctions.sh
set -x

# JSON
jsonToDhall "config.json" "config.dhall"
dhallToJson "config.dhall" "config.json"
# YAML
yamlToDhall2 "compose-dev.yaml" "compose-dev-2.dhall"
dhallToYaml "compose-dev.dhall" "compose-dev.yaml"
dhallToYaml2ng "compose-dev.dhall" "compose-dev-2ng.yaml"
