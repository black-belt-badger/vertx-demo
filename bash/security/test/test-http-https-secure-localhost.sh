#!/usr/bin/env bash
set -euxo pipefail

curl -k http://localhost:8080
curl -k --cert ../cert.pem --key ../key.pem http://localhost:8080
curl --cacert ../cert.pem --cert ../cert.pem --key ../key.pem https://localhost:8443
