#!/usr/bin/env bash
set -euo pipefail
FILE=$(readlink -f "$0")
DIR=$(dirname "${FILE}")
set -x

function yamlToDhall() {
  local INPUT=/input/"$1"
  local OUTPUT=/output/"$2"
  docker run -i --rm -v "$DIR"/input/:/input:ro -v "$DIR"/output/:/output:rw \
    dhallhaskell/dhall-yaml yaml-to-dhall --file "$INPUT" --output "$OUTPUT"
}

function dhallToYaml() {
  local INPUT=/input/"$1"
    local OUTPUT=/output/"$2"
    docker run -i --rm -v "$DIR"/input/:/input:ro -v "$DIR"/output/:/output:rw \
      dhallhaskell/dhall-yaml dhall-to-yaml-ng --file "$INPUT" --output "$OUTPUT"
}
