#!/usr/bin/env bash
set -euo pipefail
./build.sh
java -cp 'build:lib/*' grooveforge.App
