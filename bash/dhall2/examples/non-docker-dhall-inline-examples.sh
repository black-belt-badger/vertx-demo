#!/usr/bin/env bash
set -euo pipefail
set -x

dhall <<< '{ foo = [1, 2, 3], bar = True }'
echo '{ foo = [1, 2, 3], bar = True }' | dhall
