#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/quote?symbol=AAPL"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Quote-AAPL.json
