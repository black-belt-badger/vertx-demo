#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/compose/out

yamlToDhall2 compose-dev.yaml compose-dev.dhall ./data/compose
cp ./data/compose/out/compose-dev.dhall ./data/compose/in/

yamlToDhall2 compose-prod.yaml compose-prod.dhall ./data/compose
cp ./data/compose/out/compose-prod.dhall ./data/compose/in/

dhallToYaml    compose-dev.dhall  compose-dev.yaml      ./data/compose
dhallToYaml2ng compose-dev.dhall  compose-dev-2ng.yaml  ./data/compose

dhallToYaml    compose-prod.dhall compose-prod.yaml     ./data/compose
dhallToYaml2ng compose-prod.dhall compose-prod-2ng.yaml ./data/compose

rm ./data/compose/in/compose-dev.dhall
rm ./data/compose/in/compose-prod.dhall
