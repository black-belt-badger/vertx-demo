#!/usr/bin/env bash
set -euo pipefail
set -x

INPUT_JSON=./input/calendar-ipo.json
INTERMEDIATE_JSON=./output/calendar-ipo-array.json
INTERMEDIATE_DHALL=./output/calendar-ipo.dhall
OUTPUT_SQL=./output/calendar-ipo.sql

jq '.ipoCalendar // []' $INPUT_JSON > $INTERMEDIATE_JSON
json-to-dhall '(List ./input/IPO-type.dhall)' < $INTERMEDIATE_JSON > $INTERMEDIATE_DHALL
calendar-ipo-dhall-to-sql $INTERMEDIATE_DHALL $OUTPUT_SQL
export PGPASSWORD=vertx_demo_dev_password
psql -U vertx_demo_dev_user -d vertx_demo_dev_database -h localhost -f $OUTPUT_SQL
