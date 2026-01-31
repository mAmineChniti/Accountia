#!/usr/bin/env bash
set -euo pipefail

echo "Building docker images..."
docker compose build
