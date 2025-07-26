#!/usr/bin/env bash
set -euxo pipefail

curl -k http://localhost:8080
curl -k --cert ../smooth-server/server.cert.pem --key ../smooth-server/server.key.pem https://localhost:8443
curl --cacert ../smooth-ca/ca.cert.pem --cert ../smooth-server/server.cert.pem --key ../smooth-server/server.key.pem https://localhost:8443
