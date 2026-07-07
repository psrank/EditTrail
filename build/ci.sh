#!/usr/bin/env bash
set -euo pipefail

# Source of truth for local and Woodpecker CI checks.
chmod +x ./gradlew
./gradlew --no-daemon clean check buildPlugin
