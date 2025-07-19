#!/usr/bin/env bash
set -euo pipefail
set -x

dhall format ./imports/compose/v3/defaults.dhall
dhall format ./imports/compose/v3/package.dhall
dhall format ./imports/compose/v3/types.dhall
dhall format ./imports/vertx-demo-config/vdc.dhall
dhall format ./input/compose.dhall

dhall-to-yaml-ng <<< "(./input/compose.dhall).dev" --output ./output/compose-dev.yaml
dhall-to-yaml-ng <<< "(./input/compose.dhall).prod" --output ./output/compose-prod.yaml
