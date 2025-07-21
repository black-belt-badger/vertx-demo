#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/financials-reported?symbol=AAPL"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Financials-As-Reported-AAPL.json
