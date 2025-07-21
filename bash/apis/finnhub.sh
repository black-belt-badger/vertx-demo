#!/usr/bin/env bash
set -euxo pipefail

FINNHUB_API_BASE_URL="https://finnhub.io/api/v1"
API_KEY="d1uqv0pr01qletnb7080d1uqv0pr01qletnb708g"
ENDPOINT="search?q=apple&exchange=US"

# 60 call a minute for free

curl "${FINNHUB_API_BASE_URL}/${ENDPOINT}&token=${API_KEY}"
