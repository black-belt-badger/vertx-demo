#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/crypto/symbol?exchange=binance"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Crypto-Symbol.json
