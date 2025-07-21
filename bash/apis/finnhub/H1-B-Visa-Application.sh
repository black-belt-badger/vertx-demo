#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/visa-application?symbol=AAPL&from=2021-01-01&to=2021-12-31"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/H1-B-Visa-Application.json
