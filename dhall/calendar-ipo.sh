#!/usr/bin/env bash
set -euo pipefail
set -x

jq '.ipoCalendar // []' ./input/calendar-ipo.json > ./output/calendar-ipo-array.json

json-to-dhall '(List ./input/IPO-type.dhall)' < ./output/calendar-ipo-array.json > ./output/calendar-ipo.dhall

export PGPASSWORD=vertx_demo_dev_password

psql -U vertx_demo_dev_user -d vertx_demo_dev_database -h localhost -f /tmp/insert_ipos.sql
