#!/usr/bin/env bash
set -euxo pipefail

set +x
FILE=$(readlink -f "$0")
DIR=$(dirname "${FILE}")
set -x

docker run --interactive --rm \
  --volume "${DIR}"/input/:/input:ro \
  dhallhaskell/dhall-json \
  dhall-to-json <<< '/input/example.dhall'
