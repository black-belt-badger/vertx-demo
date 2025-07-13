#!/usr/bin/env bash
set -euxo pipefail

set +x
FILE=$(readlink -f "$0")
DIR=$(dirname "${FILE}")
set -x

docker run --interactive --rm \
  --volume "${DIR}"/input/:/input:ro \
  --volume "${DIR}"/output/:/output:rw \
  dhallhaskell/dhall-json \
  dhall-to-json \
  --output '/output/config.json' \
  <<< '/input/config.dhall'
