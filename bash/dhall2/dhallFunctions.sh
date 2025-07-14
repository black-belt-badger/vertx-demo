#!/usr/bin/env bash
set -euo pipefail
FILE=$(readlink -f "$0")
DIR=$(dirname "$FILE")
set -x

DHALL_JSON=dhallhaskell/dhall-json

function dhallToJson() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  docker run -i --rm -v "$DIR"/in/:/in:ro -v "$DIR"/out/:/out:rw $DHALL_JSON dhall-to-json --file "$IN" --output "$OUT"
}

function dhallToJsonHelp() {
  docker run -i --rm $DHALL_JSON dhall-to-json -h > ./help/dhall-to-json.txt
}

function dhallToYaml() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  docker run -i --rm -v "$DIR"/in/:/in:ro -v "$DIR"/out/:/out:rw $DHALL_JSON dhall-to-yaml --file "$IN" --output "$OUT"
}

function dhallToYamlHelp() {
  docker run -i --rm $DHALL_JSON dhall-to-yaml -h > ./help/dhall-to-yaml.txt
}

function jsonToDhall() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  docker run -i --rm -v "$DIR"/in/:/in:ro -v "$DIR"/out/:/out:rw $DHALL_JSON json-to-dhall --file "$IN" --output "$OUT"
}

function jsonToDhallHelp() {
  docker run -i --rm $DHALL_JSON json-to-dhall -h > ./help/json-to-dhall.txt
}

DHALL_YAML=dhallhaskell/dhall-yaml

function dhallToYaml2ng() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  docker run -i --rm -v "$DIR"/in/:/in:ro -v "$DIR"/out/:/out:rw $DHALL_YAML dhall-to-yaml-ng --file "$IN" --output "$OUT"
}

function dhallToYaml2ngHelp() {
  docker run -i --rm $DHALL_YAML dhall-to-yaml-ng -h > ./help/dhall-to-yaml-ng.txt
}

function yamlToDhall2() {
  local IN=/in/"$1"
  local OUT=/out/"$2"
  docker run -i --rm -v "$DIR"/in/:/in:ro -v "$DIR"/out/:/out:rw $DHALL_YAML yaml-to-dhall --file "$IN" --output "$OUT"
}

function yamlToDhall2Help() {
  docker run -i --rm $DHALL_YAML yaml-to-dhall -h > ./help/yaml-to-dhall.txt
}

DHALL=dhallhaskell/dhall

function dhallHelp() {
  docker run -i --rm $DHALL dhall -h > ./help/dhall.txt
}

