#!/usr/bin/env bash
set -euo pipefail
set -x

function dhallToJson() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  local DATA="$3"
  docker run -i --rm \
    -v "$DATA"/in/:/in:ro \
    -v "$DATA"/out/:/out:rw \
    dhallhaskell/dhall-json \
    dhall-to-json \
    --file "$IN" \
    --output "$OUT"
}

function dhallToJsonToStdout() {
  local IN=/in/"$1"
  local DATA="$2"
  docker run -i --rm \
    -v "$DATA"/in/:/in:ro \
    dhallhaskell/dhall-json \
    dhall-to-json \
    <<< "$IN"
}

function dhallToYaml() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  local DATA="$3"
  docker run -i --rm \
    -v "$DATA"/in/:/in:ro \
    -v "$DATA"/out/:/out:rw \
    dhallhaskell/dhall-json \
    dhall-to-yaml \
    --file "$IN" \
    --output "$OUT"
}

function jsonToDhall() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  local DATA="$3"
  docker run -i --rm \
    -v "$DATA"/in/:/in:ro \
    -v "$DATA"/out/:/out:rw \
    dhallhaskell/dhall-json \
    json-to-dhall \
    --file "$IN" \
    --output "$OUT"
}

function dhallToYaml2ng() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  local DATA="$3"
  docker run -i --rm \
    -v "$DATA"/in/:/in:ro \
    -v "$DATA"/out/:/out:rw \
    dhallhaskell/dhall-yaml \
    dhall-to-yaml-ng \
    --file "$IN" \
    --output "$OUT"
}

function dhallToYaml2ngExtract() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  local DATA="$3"
  local ENV="$4"
  docker run -i --rm \
    -v "$DATA"/in/:/in:ro \
    -v "$DATA"/out/:/out:rw \
    dhallhaskell/dhall-yaml \
    dhall-to-yaml-ng <<< "($IN).$ENV" \
    --output "$OUT"
}

function yamlToDhall2() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  local DATA="$3"
  docker run -i --rm \
    -v "$DATA"/in/:/in:ro \
    -v "$DATA"/out/:/out:rw \
    dhallhaskell/dhall-yaml \
    yaml-to-dhall \
    --file "$IN" \
    --output "$OUT"
}

function dhallToDirectoryTree() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  local DATA="$3"
  docker run -i --rm \
    -v "$DATA"/in/:/in:ro \
    -v "$DATA"/out/:/out:rw \
    dhallhaskell/dhall \
    dhall to-directory-tree \
    --file "$IN" \
    --output "$OUT"
}

function dhallFormat() {
  local IN=/in/"$1"
  local DATA="$2"
  docker run -i --rm \
    -v "$DATA"/in/:/in:rw \
    dhallhaskell/dhall \
    dhall format "$IN"
}

function dhallFormat2() {
  local DATA="$1"
  local FILE="$2"
  docker run -i --rm \
    -v "$DATA":/d:rw \
    dhallhaskell/dhall \
    dhall format /d/"$FILE"
}
