#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/crypto/exchange"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Crypto-Exchanges.json
