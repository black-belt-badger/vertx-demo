#!/usr/bin/env bash
set -euo pipefail
set -x

dhall-to-json <<< ./input/example.dhall
