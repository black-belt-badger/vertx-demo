#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/market-status?exchange=US"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Market-Status-US.json
