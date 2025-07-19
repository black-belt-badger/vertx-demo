#!/usr/bin/env bash
set -euo pipefail
set -x

./compose.sh
./conf.sh
./examples.sh
./help.sh
./here-string-to-stdout.sh
./perfect.sh
