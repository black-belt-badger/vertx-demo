#!/usr/bin/env bash
set -euxo pipefail

# 100 request a month
# $10 per month for more, billed annualy

ACCESS_KEY=6285bc8faead8467fdf5a3d17912cd0e

curl "http://api.marketstack.com/v2/eod?access_key=${ACCESS_KEY}&symbols=AAPL"
