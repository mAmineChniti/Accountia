#!/usr/bin/env bash
set -euo pipefail

# Verify that each Maven-based service packages successfully.
# Exits non-zero on first failure and prints helpful output.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVICES=(
  api-gateway
  auth-ms
  business-ms
  client-ms
  expense-ms
  invoice-ms
)

echo "Verifying builds for services: ${SERVICES[*]}"

fail_count=0
for svc in "${SERVICES[@]}"; do
  pom="$ROOT_DIR/$svc/pom.xml"
  if [[ -f "$pom" ]]; then
    echo "\n==> Building $svc"
    if ! (cd "$ROOT_DIR/$svc" && mvn -B -DskipTests package); then
      echo "ERROR: build failed for $svc"
      fail_count=$((fail_count+1))
      break
    fi
  else
    echo "Skipping $svc (no pom.xml)"
  fi
done

if [[ $fail_count -gt 0 ]]; then
  echo "\nOne or more builds failed (count=$fail_count)."
  exit 1
fi

echo "\nAll specified services packaged successfully."
exit 0
