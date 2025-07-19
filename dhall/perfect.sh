#!/usr/bin/env bash
set -euo pipefail
set -x

dhall-to-yaml-ng --file ./input/perfect.dhall --output ./output/perfect.yaml
