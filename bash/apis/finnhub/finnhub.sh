#!/usr/bin/env bash
set -euxo pipefail

# 60 call a minute for free

export BASE_URL="https://finnhub.io/api/v1"
export API_KEY="d1uqv0pr01qletnb7080d1uqv0pr01qletnb708g"
export AUTH="X-Finnhub-Token: ${API_KEY}"
