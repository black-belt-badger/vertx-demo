#!/usr/bin/env bash
set -euo pipefail
source dhallFunctions.sh
set -x

yamlToDhall "compose-dev.yaml" "compose-dev.dhall"
