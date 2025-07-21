#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/earnings?symbol=AAPL"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Earnings-Surprises-AAPL.json
