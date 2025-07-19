#!/usr/bin/env bash
set -euo pipefail
set -x

dhall-to-json -h > ./help/dhall-to-json.txt
dhall-to-yaml -h > ./help/dhall-to-yaml.txt
json-to-dhall -h > ./help/json-to-dhall.txt
dhall-to-yaml-ng -h > ./help/dhall-to-yaml-ng.txt
yaml-to-dhall -h > ./help/yaml-to-dhall.txt

dhall -h > ./help/dhall.txt
dhall format -h > ./help/dhall-format.txt
