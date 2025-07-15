#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

# rm -fr ./data/compose2/out/

dhallToYaml2ng compose-dev.dhall compose-dev.yaml ./data/compose2
mkdir ./data/compose2/out/configs/dev
dhallToJson conf.dhall configs/dev/conf.json ./data/compose2
