#!/usr/bin/env bash
set -euo pipefail
set -x

./compose.sh
./conf.sh
./help.sh
