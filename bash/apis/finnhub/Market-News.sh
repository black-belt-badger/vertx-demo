#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/news?category=general"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Market-News-general.json
