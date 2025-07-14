#!/usr/bin/env bash
set -euo pipefail
source ../bin/conversion-functions.sh
set -x

dhallToJson "example-json.dhall" "example-json.json"
dhallToYaml "example-yaml.dhall" "example-yaml.yaml"
jsonToDhall "example-json.json" "example-json.dhall"
dhallToYaml2ng "example-yaml.dhall" "example-yaml-2.yaml"
yamlToDhall2 "example-yaml.yaml" "example-yaml.dhall"
