#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/insider-transactions?symbol=TSLA&limit=20"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Insider-AAPL-Transactions-general.json
