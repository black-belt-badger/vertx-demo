#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/calendar/ipo?from=2000-01-01&to=2026-01-01"

#curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/IPO-Calendar.json
curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/IPO-Calendar-entire.json
