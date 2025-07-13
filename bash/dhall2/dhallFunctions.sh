#!/usr/bin/env bash
set -euo pipefail
FILE=$(readlink -f "$0")
DIR=$(dirname "$FILE")
set -x

function yamlToDhall() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  docker run -i --rm -v "$DIR"/in/:/in:ro -v "$DIR"/out/:/out:rw \
    dhallhaskell/dhall-yaml yaml-to-dhall --file "$IN" --output "$OUT"
}

function dhallToYaml() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  docker run -i --rm -v "$DIR"/in/:/in:ro -v "$DIR"/out/:/out:rw \
    dhallhaskell/dhall-yaml dhall-to-yaml-ng --file "$IN" --output "$OUT"
}
