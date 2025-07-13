#!/usr/bin/env bash
set -euxo pipefail

set +x
FILE=$(readlink -f "$0")
DIR=$(dirname "${FILE}")
set -x

docker run --interactive --rm \
  --volume "${DIR}"/input/:/input:ro \
  --volume "${DIR}"/output/:/output:rw \
  dhallhaskell/dhall-yaml \
  dhall-to-yaml-ng \
  --output '/output/example-from-web.yaml' \
  <<< '/input/example-from-web.dhall'
