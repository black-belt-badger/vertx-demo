#!/usr/bin/env bash
set -euo pipefail
source ./bin/conversion-functions.sh
set -x

rm -fr ./data/experiment/out/

dhallFormat experiment.dhall ./data/experiment
dhallFormat2 ./data/experiment/in/imports/Job/ default.dhall
dhallFormat2 ./data/experiment/in/imports/Job/ package.dhall
dhallFormat2 ./data/experiment/in/imports/Job/ toJSON.dhall
dhallFormat2 ./data/experiment/in/imports/Job/ Type.dhall
dhallFormat2 ./data/experiment/in/imports/ dropNones.dhall
dhallFormat2 ./data/experiment/in/imports/ Prelude.dhall

dhallToJson experiment.dhall experiment.json ./data/experiment/
