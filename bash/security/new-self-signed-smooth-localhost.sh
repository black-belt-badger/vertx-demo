#!/usr/bin/env bash
set -euxo pipefail

openssl req -x509 -out cert.pem -keyout key.pem \
  -newkey rsa:2048 -nodes -sha256 \
  -subj '/CN=localhost' \
  -addext 'subjectAltName=DNS:localhost' \
  -days 365
