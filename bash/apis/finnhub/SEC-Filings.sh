#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/filings?symbol=AAPL"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/SEC-Filings-AAPL.json
