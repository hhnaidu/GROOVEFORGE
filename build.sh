#!/usr/bin/env bash
set -euo pipefail
mkdir -p build
javac -source 8 -target 8 -cp 'lib/*' -d build src/grooveforge/Model.java src/grooveforge/TestConnection.java src/grooveforge/App.java
echo 'Build successful.'
