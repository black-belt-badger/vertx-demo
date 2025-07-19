#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/compose/out/*

dhallFormat2 ./data/compose/in/imports/compose/v3 defaults.dhall
dhallFormat2 ./data/compose/in/imports/compose/v3 package.dhall
dhallFormat2 ./data/compose/in/imports/compose/v3 types.dhall
dhallFormat2 ./data/compose/in/imports/vertx-demo-config vdc.dhall

dhallFormat compose.dhall ./data/compose
dhallToYaml2ngExtract compose.dhall compose-dev.yaml  ./data/compose dev
dhallToYaml2ngExtract compose.dhall compose-prod.yaml ./data/compose prod

mkdir -p ./data/compose/out/configs/dev
dhallFormat conf.dhall ./data/compose
dhallToJson conf.dhall configs/conf.json ./data/compose
