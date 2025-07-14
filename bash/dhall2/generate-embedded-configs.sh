#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

dhallToJson "config.dhall" "config.json" ./data
#dhallToDirectoryTree "configs.dhall" "embedded-configs" ./data
