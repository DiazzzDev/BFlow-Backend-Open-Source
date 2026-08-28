#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"

source "$ROOT/config.env"

DB_PROVIDER="${DB_PROVIDER:-rds}"

echo "Starting infrastructure bootstrap (DB_PROVIDER=${DB_PROVIDER})..."

for script in \
    "$ROOT/bootstrap/01-vpc.sh" \
    "$ROOT/bootstrap/02-subnets.sh" \
    "$ROOT/bootstrap/03-internet-gateway.sh" \
    "$ROOT/bootstrap/04-route-tables.sh" \
    "$ROOT/bootstrap/05-security-groups.sh" \
    "$ROOT/bootstrap/06-ecr.sh" \
    "$ROOT/bootstrap/07-rds.sh" \
    "$ROOT/bootstrap/08-secrets.sh" \
    "$ROOT/bootstrap/09-iam.sh" \
    "$ROOT/bootstrap/10-cloudwatch.sh" \
    "$ROOT/bootstrap/11-ecs.sh" \
    "$ROOT/bootstrap/12-github-oidc.sh" \
    "$ROOT/bootstrap/13-budget.sh" \
    "$ROOT/bootstrap/14-ocr-pipeline.sh"
do
    SCRIPT_NAME="$(basename "$script")"

    if [[ "$SCRIPT_NAME" == "07-rds.sh" && "$DB_PROVIDER" != "rds" ]]; then
        echo
        echo "=================================================="
        echo "Skipping $SCRIPT_NAME (DB_PROVIDER=$DB_PROVIDER, not 'rds')"
        echo "=================================================="
        continue
    fi

    echo
    echo "=================================================="
    echo "Running $SCRIPT_NAME"
    echo "=================================================="

    bash "$script"
done

echo
echo "Infrastructure ready."
