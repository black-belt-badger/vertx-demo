#!/usr/bin/env bash
set -euxo pipefail

curl -k http://0.0.0.0:8080
curl -k https://0.0.0.0:8443
