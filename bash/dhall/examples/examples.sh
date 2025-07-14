#!/usr/bin/env bash
set -euo pipefail
source ../bin/conversion-functions.sh
set -x

rm -fr ./data/examples/out/

dhallToJson example-json.dhall example-json.json ./data/examples
dhallToYaml example-yaml.dhall example-yaml.yaml ./data/examples
jsonToDhall example-json.json example-json.dhall ./data/examples
dhallToYaml2ng example-yaml.dhall example-yaml-2.yaml ./data/examples
yamlToDhall2 example-yaml.yaml example-yaml.dhall ./data/examples

dhallToJson example-with-code.dhall example-with-code.json ./data/examples

docker run --interactive --rm dhallhaskell/dhall-json dhall-to-json <<< '{ x = 1, y = True }'
docker run --interactive --rm dhallhaskell/dhall-json dhall-to-yaml <<< '{ x = 1, y = True }'
docker run --interactive --rm dhallhaskell/dhall-json json-to-dhall <<< '{ "x": 1, "y": true }'
