#!/usr/bin/env bash
set -euo pipefail
source ../bin/conversion-functions.sh
set -x

rm -fr ./data/perfect/out/

dhallToYaml2ng perfect.dhall perfect.yaml ./data/perfect
