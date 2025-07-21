#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/forex/symbol?exchange=oanda"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Forex-Symbol.json
