#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/lobbying?symbol=AAPL&from=2021-01-01&to=2022-12-31"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Senate-Lobbying.json
