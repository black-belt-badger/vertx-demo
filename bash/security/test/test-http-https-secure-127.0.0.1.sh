#!/usr/bin/env bash
set -euxo pipefail

curl -k http://127.0.0.1:8080
curl -k --cert ../cert.pem --key ../key.pem https://127.0.0.1:8443
curl --cacert ../cert.pem --cert ../cert.pem --key ../key.pem https://127.0.0.1:8443
