#!/usr/bin/env bash
set -euo pipefail
source dhallFunctions.sh
set -x

dhallToJson "config.dhall" "config.json"
dhallToYaml "compose-dev.dhall" "compose-dev.yaml"
jsonToDhall "config.json" "config.dhall"
dhallToYaml2ng "compose-dev.dhall" "compose-dev-2ng.yaml"
yamlToDhall2 "compose-dev.yaml" "compose-dev-2.dhall"

dhallToJsonHelp
dhallToYamlHelp
jsonToDhallHelp
dhallToYaml2ngHelp
yamlToDhall2Help
dhallHelp
