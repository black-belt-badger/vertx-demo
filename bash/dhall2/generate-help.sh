#!/usr/bin/env bash
set -euo pipefail
source ./bin/help-functions.sh
set -x

dhallToJsonHelp
dhallToYamlHelp
jsonToDhallHelp
dhallToYaml2ngHelp
yamlToDhall2Help
dhallHelp
