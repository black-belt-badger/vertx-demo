#!/usr/bin/env bash
set -euo pipefail
source ./finnhub.sh
set -x

INPUT_JSON=./input/news_merger.json
INTERMEDIATE_DHALL=./output/news_merger.dhall
OUTPUT_SQL=./output/news_merger.sql

REQUEST="/news?category=merger"
curl -H "${AUTH}" "${BASE_URL}/${REQUEST}" > $INPUT_JSON

json-to-dhall '(List ./input/News-type.dhall)' < $INPUT_JSON > $INTERMEDIATE_DHALL
calendar-ipo-dhall-to-sql news_general $INTERMEDIATE_DHALL $OUTPUT_SQL
export PGPASSWORD=vertx_demo_dev_password
psql -U vertx_demo_dev_user -d vertx_demo_dev_database -h localhost -f $OUTPUT_SQL
