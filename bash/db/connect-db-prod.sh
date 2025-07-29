#!/usr/bin/env bash
set -euo pipefail
set -x

USER=vertx_demo_admin
PASS=vertx_demo_password
HOST=vertx-demo-db.chimcku4qngw.eu-north-1.rds.amazonaws.com
HOST=grove-db.chimcku4qngw.eu-north-1.rds.amazonaws.com
DATABASE=postgres

export PGPASSWORD=$PASS

PGPASSWORD=$PASS psql "host=$HOST port=5432 dbname=$DATABASE user=$USER sslmode=require" 

