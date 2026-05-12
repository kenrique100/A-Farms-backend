#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if ! docker network inspect dev-network >/dev/null 2>&1; then
  echo "Creating shared external network: dev-network"
  docker network create dev-network >/dev/null
fi

for service_dir in \
  income-service \
  expense-service \
  investment-service \
  transaction-service \
  user-service \
  api-gateway; do
  echo "Starting ${service_dir}..."
  docker compose -f "${ROOT_DIR}/${service_dir}/docker-compose.yml" up -d --build
  echo "${service_dir} started"
done

echo "All services started on dev-network."
