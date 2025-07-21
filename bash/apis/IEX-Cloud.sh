#!/usr/bin/env bash
set -euxo pipefail

API_TOKEN=???

URL="https://cloud.iexapis.com/stable/stock/AAPL/quote?token=$API_TOKEN"

curl $URL
