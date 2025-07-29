#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/calendar/earnings?from=2025-07-29&to=2025-08-31"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Earnings-Calendar-AAPL.json

#GET https://finnhub.io/api/v1/calendar/earnings?from=2025-07-29&to=2025-08-31
