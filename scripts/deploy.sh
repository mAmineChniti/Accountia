#!/usr/bin/env bash
set -euo pipefail

echo "Bringing services up..."
docker compose up -d --remove-orphans
