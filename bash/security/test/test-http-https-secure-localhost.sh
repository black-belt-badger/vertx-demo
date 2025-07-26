#!/usr/bin/env bash
set -euxo pipefail

curl -k http://localhost:8080
curl -k --cert ../certs/cert-localhost/cert.pem --key ../certs/cert-localhost/key.pem http://localhost:8080
curl --cacert ../certs/cert-localhost/cert.pem --cert ../certs/cert-localhost/cert.pem --key ../certs/cert-localhost/key.pem https://localhost:8443
