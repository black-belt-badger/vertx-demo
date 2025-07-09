#!/usr/bin/env bash
set -euxo pipefail

aws logs put-log-events --log-group-name training-log-group --log-stream-name training-log-stream --log-events file://events.json
