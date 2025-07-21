#!/usr/bin/env bash
set -euxo pipefail

# 25 requests per day for free
# 75 requests per minute for $50 a month or $500 a year
# trading courses for free

API_KEY=49A9OCDVTLWTEA1T

curl "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=IBM&interval=5min&apikey=${API_KEY}"
