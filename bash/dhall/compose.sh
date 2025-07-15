#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/compose/out/*

dhallFormat compose.dhall ./data/compose
dhallToYaml2ngExtract compose.dhall compose-dev.yaml  ./data/compose dev
dhallToYaml2ngExtract compose.dhall compose-prod.yaml ./data/compose prod

mkdir -p ./data/compose/out/configs/dev
dhallFormat conf.dhall ./data/compose
dhallToJson conf.dhall configs/conf.json ./data/compose
