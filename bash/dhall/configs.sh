#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/configs/out/

dhallFormat configs.dhall ./data/configs
dhallToDirectoryTree configs.dhall conf ./data/configs/
