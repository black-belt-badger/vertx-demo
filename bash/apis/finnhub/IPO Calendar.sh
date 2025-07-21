#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/calendar/ipo?from=2020-01-01&to=2020-04-30"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/IPO-Calendar.json
