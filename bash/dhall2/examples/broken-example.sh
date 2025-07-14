#!/usr/bin/env bash
set -euo pipefail
source ../bin/conversion-functions.sh
set -x

rm -fr ./data/broken/out/
dhallToDirectoryTree "broken.dhall" "compose" ./data/broken

