#!/usr/bin/env bash
set -euo pipefail
set -x

dhall-to-json --file ./input/example-json.dhall --output ./output/example-json.json
dhall-to-yaml --file ./input/example-json.dhall --output ./output/example-json.yaml
json-to-dhall --file ./input/example-json.json --output ./output/example-dhall.dhall
dhall-to-yaml-ng --file ./input/example-yaml.dhall --output ./output/example-yaml.yaml
yaml-to-dhall --file ./input/example-yaml.yaml --output ./output/example-yaml.dhall

dhall-to-json --file ./input/example-with-code.dhall --output ./output/example-with-code.json

dhall-to-json <<< '{ x = 1, y = True }'
dhall-to-yaml <<< '{ x = 1, y = True }'
json-to-dhall <<< '{ "x": 1, "y": true }'

dhall <<< '{ foo = [1, 2, 3], bar = True }'
echo '{ foo = [1, 2, 3], bar = True }' | dhall
