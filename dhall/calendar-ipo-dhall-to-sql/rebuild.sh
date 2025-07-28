#!/usr/bin/env bash
set -euo pipefail
set -x

stack clean && stack build && stack install
