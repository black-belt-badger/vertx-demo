#!/usr/bin/env bash
set -euxo pipefail

docker run --interactive --rm dhallhaskell/dhall-json dhall-to-json --help
