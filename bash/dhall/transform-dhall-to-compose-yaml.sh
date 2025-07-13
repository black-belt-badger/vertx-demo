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
  --file '/input/compose-dev.dhall' \
  --output '/output/compose-dev-2.yaml'

docker run --interactive --rm \
  --volume "${DIR}"/input/:/input:ro \
  --volume "${DIR}"/output/:/output:rw \
  dhallhaskell/dhall-yaml \
  dhall-to-yaml-ng \
  --file '/input/compose-prod.dhall' \
  --output '/output/compose-prod-2.yaml'
