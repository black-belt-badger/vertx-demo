#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/recommendation?symbol=AAPL"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Recommendation-Trends-AAPL.json
