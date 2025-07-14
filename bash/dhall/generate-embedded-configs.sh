#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

dhallToDirectoryTree "configs.dhall" "embedded-configs" ./data
