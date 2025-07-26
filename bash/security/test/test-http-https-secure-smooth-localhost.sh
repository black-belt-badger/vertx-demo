#!/usr/bin/env bash
set -euxo pipefail

curl -k http://localhost:8080
curl -k --cert ../certs/localhost-smooth/cert.pem --key ../certs/localhost-smooth/key.pem https://localhost:8443
curl --cacert ../certs/localhost-smooth/cert.pem --cert ../certs/localhost-smooth/cert.pem --key ../certs/localhost-smooth/key.pem https://localhost:8443
