#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/stock/uspto-patent?symbol=NVDA&from=2021-01-01&to=2021-12-31"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/USPTO-Patents.json
