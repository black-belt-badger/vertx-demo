#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/config/out/

dhallFormat config.dhall ./data/config
dhallToJson config.dhall config.json ./data/config

dhallFormat configs.dhall ./data/config
dhallToDirectoryTree configs.dhall config ./data/config
