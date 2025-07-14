#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/embedded-configs/out/
dhallToDirectoryTree configs.dhall embedded-configs ./data/embedded-configs/
