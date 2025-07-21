#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/symbol?exchange=US"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Stock-Symbols-US.json
