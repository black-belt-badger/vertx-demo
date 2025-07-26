#!/usr/bin/env bash
set -euxo pipefail

curl -k http://0.0.0.0:8080
curl -k --cert ../cert.pem --key ../key.pem https://0.0.0.0:8443
curl --cacert ../cert.pem --cert ../cert.pem --key ../key.pem https://0.0.0.0:8443
