#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/config/out/
dhallToJson config.dhall config.json ./data/config
