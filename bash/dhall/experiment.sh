#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/experiment/out/

#dhallFormat experiment.dhall ./data/experiment
dhallToJson experiment.dhall experiment.json ./data/experiment/
