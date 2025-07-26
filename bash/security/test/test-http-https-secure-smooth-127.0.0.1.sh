#!/usr/bin/env bash
set -euxo pipefail

curl -k http://127.0.0.1:8080
curl -k --cert ../certs/localhost-smooth/cert.pem --key ../certs/localhost-smooth/key.pem https://127.0.0.1:8443
curl --cacert ../certs/localhost-smooth/cert.pem --cert ../certs/localhost-smooth/cert.pem --key ../certs/localhost-smooth/key.pem https://127.0.0.1:8443
