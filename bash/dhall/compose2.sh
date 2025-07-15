#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/compose2/out/

dhallToYaml2ngExtract compose.dhall compose-dev.yaml  ./data/compose2 dev
dhallToYaml2ngExtract compose.dhall compose-prod.yaml ./data/compose2 prod

mkdir -p ./data/compose2/out/configs/dev
dhallToJson conf.dhall configs/dev/conf.json ./data/compose2
