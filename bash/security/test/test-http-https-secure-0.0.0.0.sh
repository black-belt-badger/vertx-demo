#!/usr/bin/env bash
set -euxo pipefail

curl -k http://0.0.0.0:8080
curl -k --cert ../certs/cert-localhost/cert.pem --key ../certs/cert-localhost/key.pem https://0.0.0.0:8443
curl --cacert ../certs/cert-localhost/cert.pem --cert ../certs/cert-localhost/cert.pem --key ../certs/cert-localhost/key.pem https://0.0.0.0:8443
