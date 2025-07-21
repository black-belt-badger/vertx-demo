#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/country"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Country-Metadata.json
