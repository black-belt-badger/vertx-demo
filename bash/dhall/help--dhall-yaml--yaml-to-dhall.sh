#!/usr/bin/env bash
set -euxo pipefail

docker run --interactive --rm dhallhaskell/dhall-yaml yaml-to-dhall --help
