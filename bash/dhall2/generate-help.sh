#!/usr/bin/env bash
set -euo pipefail
source dhallFunctions.sh
set -x

dhallToJsonHelp
dhallToYamlHelp
jsonToDhallHelp
dhallToYaml2ngHelp
yamlToDhall2Help
dhallHelp
