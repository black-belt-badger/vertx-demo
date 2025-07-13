#!/usr/bin/env bash
set -euxo pipefail

set +x
FILE=$(readlink -f "$0")
DIR=$(dirname "${FILE}")
set -x

dhall to-directory-tree --file "${DIR}"/input/compose.dhall --output "${DIR}/output"
