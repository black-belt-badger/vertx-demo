#!/usr/bin/env bash
set -euxo pipefail

set +x
FILE=$(readlink -f "$0")
DIR=$(dirname "${FILE}")
set -x

docker run --interactive --rm \
  --volume "${DIR}"/data/:/data \
  dhallhaskell/dhall-json \
  dhall-to-json \
  --output '/data/example-with-code.json' \
  <<< '/data/example-with-code.dhall'
