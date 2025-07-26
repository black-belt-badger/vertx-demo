#!/usr/bin/env bash
set -euxo pipefail

openssl x509 -in ./cert.pem -text -noout
openssl rsa -in ./key.pem -check
