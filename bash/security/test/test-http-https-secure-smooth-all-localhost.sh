#!/usr/bin/env bash
set -euxo pipefail

curl -f --output /dev/null -k http://localhost:8080
curl -f --output /dev/null -k --cert ../smooth-server/server.cert.pem --key ../smooth-server/server.key.pem https://localhost:8443
curl -f --output /dev/null --cacert ../smooth-ca/ca.cert.pem --cert ../smooth-server/server.cert.pem --key ../smooth-server/server.key.pem https://localhost:8443
