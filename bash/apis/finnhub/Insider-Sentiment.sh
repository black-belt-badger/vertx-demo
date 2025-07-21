#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/insider-sentiment?symbol=TSLA&from=2015-01-01&to=2022-03-01"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Insider-AAPL-Sentiment-TSLA.json
