#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

INPUT_JSON=./input/news_general.json
INTERMEDIATE_DHALL=./output/news_general.dhall
OUTPUT_SQL=./output/news_general.sql

REQUEST="/news?category=general"
curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > $INPUT_JSON

json-to-dhall '(List ./input/News-type.dhall)' < $INPUT_JSON > $INTERMEDIATE_DHALL
calendar-ipo-dhall-to-sql news_general $INTERMEDIATE_DHALL $OUTPUT_SQL
export PGPASSWORD=vertx_demo_dev_password
psql -U vertx_demo_dev_user -d vertx_demo_dev_database -h localhost -f $OUTPUT_SQL
