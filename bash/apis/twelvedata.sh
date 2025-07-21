#!/usr/bin/env bash
set -euxo pipefail

# 800 request a day

SECRET_KEY=b97984d855ec450a9b253af62cac81e0

curl "https://api.twelvedata.com/price?symbol=AAPL&apikey=$SECRET_KEY"
