#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/fda-advisory-committee-calendar"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/FDA-Committee-Meeting-Calendar.json
