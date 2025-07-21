#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/forex/exchange"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Forex-Exchanges.json
