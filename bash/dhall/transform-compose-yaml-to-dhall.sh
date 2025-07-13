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
  yaml-to-dhall \
  --file '/input/compose-dev.yaml' \
  --output '/output/compose-dev.dhall'

docker run --interactive --rm \
  --volume "${DIR}"/input/:/input:ro \
  --volume "${DIR}"/output/:/output:rw \
  dhallhaskell/dhall-yaml \
  yaml-to-dhall \
  --file '/input/compose-prod.yaml' \
  --output '/output/compose-prod.dhall'
