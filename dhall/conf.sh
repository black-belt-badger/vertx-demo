#!/usr/bin/env bash
set -euo pipefail
set -x

dhall format ./input/configs.dhall

dhall to-directory-tree --file ./input/configs.dhall --output ./output/
