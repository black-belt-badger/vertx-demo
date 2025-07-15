#!/usr/bin/env bash
set -euo pipefail
FILE=$(readlink -f "$0")
DIR=$(dirname "$FILE")
set -x

HELP=$DIR/help

DHALL_JSON=dhallhaskell/dhall-json

function dhallToJsonHelp() {
  docker run -i --rm $DHALL_JSON dhall-to-json -h > $HELP/dhall-to-json.txt
}

function dhallToYamlHelp() {
  docker run -i --rm $DHALL_JSON dhall-to-yaml -h > $HELP/dhall-to-yaml.txt
}

function jsonToDhallHelp() {
  docker run -i --rm $DHALL_JSON json-to-dhall -h > $HELP/json-to-dhall.txt
}

DHALL_YAML=dhallhaskell/dhall-yaml

function dhallToYaml2ngHelp() {
  docker run -i --rm $DHALL_YAML dhall-to-yaml-ng -h > $HELP/dhall-to-yaml-ng.txt
}

function yamlToDhall2Help() {
  docker run -i --rm $DHALL_YAML yaml-to-dhall -h > $HELP/yaml-to-dhall.txt
}

DHALL=dhallhaskell/dhall

function dhallHelp() {
  docker run -i --rm $DHALL dhall -h > $HELP/dhall.txt
}

function dhallFormatHelp() {
  docker run -i --rm $DHALL dhall format -h > $HELP/dhall--format.txt
}

