#!/usr/bin/env bash
set -euxo pipefail

set +x
FILE=$(readlink -f "$0")
DIR=$(dirname "${FILE}")
set -x

docker run --interactive --rm dhallhaskell/dhall-json dhall-to-json --help
