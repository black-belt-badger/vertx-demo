#!/usr/bin/env bash
set -euxo pipefail

dhall <<< '{ foo = [1, 2, 3], bar = True }'
echo '{ foo = [1, 2, 3], bar = True }' | dhall

docker run --interactive --rm dhallhaskell/dhall-json dhall-to-json <<< '{ x = 1, y = True }'
docker run --interactive --rm dhallhaskell/dhall-json dhall-to-yaml <<< '{ x = 1, y = True }'
docker run --interactive --rm dhallhaskell/dhall-json json-to-dhall <<< '{ "x": 1, "y": true }'
