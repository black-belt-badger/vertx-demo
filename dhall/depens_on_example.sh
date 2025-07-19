#!/usr/bin/env bash
set -euo pipefail
set -x

#yaml-to-dhall type --file ./input/depends_on_example.dhall

dhall format ./imports/compose/v3/package.dhall
dhall format ./imports/compose/v3/types.dhall
dhall format ./imports/compose/v3/defaults.dhall
dhall format ./input/simplest.dhall
dhall-to-yaml --file ./input/simplest.dhall
dhall-to-yaml-ng <<< "(./input/simplest.dhall).dev"
