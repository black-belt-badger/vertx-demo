#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

yamlToDhall2 "compose-dev.yaml" "compose-dev.dhall" "./data/compose"
cp ./data/compose/out/compose-dev.dhall ./data/compose/in/

yamlToDhall2 "compose-prod.yaml" "compose-prod.dhall" "./data/compose"
cp ./data/compose/out/compose-prod.dhall ./data/compose/in/
