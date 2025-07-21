#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/calendar/earnings?symbol=AAPL"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Earnings-Calendar-AAPL.json
