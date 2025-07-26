#!/usr/bin/env bash
set -euxo pipefail

#!/bin/bash

mkdir -p smooth-ca
mkdir -p smooth-server

# 1. Create a self-signed root CA key and cert
openssl genrsa -out smooth-ca/ca.key.pem 2048
openssl req -x509 -new -nodes -key smooth-ca/ca.key.pem \
  -sha256 -days 825 -out smooth-ca/ca.cert.pem \
  -subj "/C=XX/ST=Local/L=Localhost/O=MyOrg/OU=Dev/CN=MyFakeCA"

# 2. Create a private key for the server
openssl genrsa -out smooth-server/server.key.pem 2048

# 3. Create a config with SANs
cat > smooth-server/localhost.cnf <<EOF
[req]
default_bits = 2048
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn

[dn]
C = XX
ST = Local
L = Localhost
O = Dev
OU = Dev
CN = localhost

[req_ext]
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
IP.1 = 127.0.0.1
IP.2 = 0.0.0.0
EOF

# 4. Create a certificate signing request (CSR)
openssl req -new -key smooth-server/server.key.pem \
  -out smooth-server/server.csr.pem \
  -config smooth-server/localhost.cnf

# 5. Sign server cert with the CA
openssl x509 -req -in smooth-server/server.csr.pem \
  -CA smooth-ca/ca.cert.pem -CAkey smooth-ca/ca.key.pem -CAcreateserial \
  -out smooth-server/server.cert.pem -days 825 -sha256 \
  -extfile smooth-server/localhost.cnf -extensions req_ext
