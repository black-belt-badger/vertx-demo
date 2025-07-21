#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

REQUEST="search?q=apple&exchange=US"

curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > output/Symbol-Lookup-apple-US.json
