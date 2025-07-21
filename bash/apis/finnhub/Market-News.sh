#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="/news?category=merger"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Market-News-merger.json
