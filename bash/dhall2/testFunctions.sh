#!/usr/bin/env bash
set -euo pipefail
source dhallFunctions.sh
set -x

jsonToDhall "config.json" "config.dhall"
dhallToJson "config.dhall" "config.json"
yamlToDhall "compose-dev.yaml" "compose-dev.dhall"
dhallToYaml "compose-dev.dhall" "compose-dev.yaml"
