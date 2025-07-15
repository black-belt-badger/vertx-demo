#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/compose-legacy/out

yamlToDhall2 compose-dev.yaml compose-dev.dhall ./data/compose-legacy
cp ./data/compose-legacy/out/compose-dev.dhall ./data/compose-legacy/in/

yamlToDhall2 compose-prod.yaml compose-prod.dhall ./data/compose-legacy
cp ./data/compose-legacy/out/compose-prod.dhall ./data/compose-legacy/in/

dhallToYaml    compose-dev.dhall  compose-dev.yaml      ./data/compose-legacy
dhallToYaml2ng compose-dev.dhall  compose-dev-2ng.yaml  ./data/compose-legacy

dhallToYaml    compose-prod.dhall compose-prod.yaml     ./data/compose-legacy
dhallToYaml2ng compose-prod.dhall compose-prod-2ng.yaml ./data/compose-legacy

rm ./data/compose-legacy/in/compose-dev.dhall
rm ./data/compose-legacy/in/compose-prod.dhall
