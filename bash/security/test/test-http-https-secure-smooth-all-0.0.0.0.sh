#!/usr/bin/env bash
set -euxo pipefail

curl -k http://0.0.0.0:8080
curl -k --cert ../smooth-server/server.cert.pem --key ../smooth-server/server.key.pem https://0.0.0.0:8443
curl --cacert ../smooth-ca/ca.cert.pem --cert ../smooth-server/server.cert.pem --key ../smooth-server/server.key.pem https://0.0.0.0:8443
