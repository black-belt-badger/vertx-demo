#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

dhallToJsonToStdout "example.dhall" ignored ./data/example
