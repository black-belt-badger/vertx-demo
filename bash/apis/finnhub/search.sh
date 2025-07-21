#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="search?q=apple&exchange=US"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/search-apple-US.json
