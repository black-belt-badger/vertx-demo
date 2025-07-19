#!/usr/bin/env bash
set -euo pipefail
set -x

yaml-to-dhall type --file ./input/depends_on_example.yaml

dhall format ./imports/compose/v3/package.dhall
dhall format ./imports/compose/v3/types.dhall
dhall format ./imports/compose/v3/defaults.dhall
dhall format ./input/simplest.yaml
dhall-to-yaml --file ./input/simplest.yaml
