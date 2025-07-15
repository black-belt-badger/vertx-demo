#!/usr/bin/env bash
set -euo pipefail
FILE=$(readlink -f "$0")
DIR=$(dirname "$FILE")
set -x

bash "$DIR"/compose.sh
bash "$DIR"/compose-legacy.sh
bash "$DIR"/config.sh
bash "$DIR"/configs.sh
bash "$DIR"/help.sh
